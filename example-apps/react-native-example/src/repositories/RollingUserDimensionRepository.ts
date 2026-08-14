/**
 * A realistic, rolling-window `UserDimensionRepository` reference implementation.
 *
 * `MockRepositories.ts` in this example returns flat, hand-picked values so the
 * automated scenario test suite (`src/tests/testScenarios.ts`) can assert exact
 * outcomes — it is NOT meant to model real engagement over time. This file is
 * the other half of the picture: a day-bucketed rolling-window tracker you can
 * adapt for a production app, following the same algorithm Sophi's web SDK uses
 * (see the full integration guide for the reference web implementation).
 *
 * It is intentionally NOT wired into App.tsx or the test suite.
 *
 * Storage is abstracted behind `KeyValueStore` so this file has no dependency
 * on any particular persistence library. In a real app, back it with
 * `@react-native-async-storage/async-storage`, MMKV, or similar — anything
 * that can get/set a string by key.
 */
import type { DeviceDimensionRepository, DeviceDimensions, UserDimensionRepository, UserDimensions } from '@mather-sophi/paywall';

export interface KeyValueStore {
  get(key: string): Promise<string | null>;
  set(key: string, value: string): Promise<void>;
}

/** In-memory KeyValueStore for demonstration/testing — data does not survive an app restart. */
export class InMemoryKeyValueStore implements KeyValueStore {
  private store = new Map<string, string>();

  async get(key: string): Promise<string | null> {
    return this.store.has(key) ? this.store.get(key)! : null;
  }

  async set(key: string, value: string): Promise<void> {
    this.store.set(key, value);
  }
}

type VisitorType = 'anonymous' | 'registered';

interface DayBucket {
  pageViews: number;
  pageViewsByArticle: number;
  pageViewsByArticleWithPaywall: number;
  pageViewsByArticleWithRegwall: number;
  topLevelSections: string[];
  topLevelSectionsByArticle: string[];
}

function emptyDayBucket(): DayBucket {
  return {
    pageViews: 0,
    pageViewsByArticle: 0,
    pageViewsByArticleWithPaywall: 0,
    pageViewsByArticleWithRegwall: 0,
    topLevelSections: [],
    topLevelSectionsByArticle: [],
  };
}

/** today + up to 28 days of history */
const MAX_STORED_DAYS = 29;

const DAY_BUCKETS_KEY = 'sophi.dayBuckets';
const LAST_SEEN_DATE_KEY = 'sophi.lastSeenDate';
const VISITOR_TYPE_KEY = 'sophi.visitorType';

function todayDateString(): string {
  return new Date().toISOString().slice(0, 10); // "YYYY-MM-DD"
}

function daysBetween(lastSeenDate: string): number {
  if (!lastSeenDate) return 0;
  const then = new Date(`${lastSeenDate}T00:00:00Z`).getTime();
  const now = new Date(`${todayDateString()}T00:00:00Z`).getTime();
  return Math.max(0, Math.round((now - then) / (1000 * 60 * 60 * 24)));
}

interface WindowSum {
  pageViews: number;
  pageViewsByArticle: number;
  pageViewsByArticleWithPaywall: number;
  pageViewsByArticleWithRegwall: number;
  topLevelSections: number;
  topLevelSectionsByArticle: number;
  visitCount: number;
}

function emptyWindowSum(): WindowSum {
  return {
    pageViews: 0,
    pageViewsByArticle: 0,
    pageViewsByArticleWithPaywall: 0,
    pageViewsByArticleWithRegwall: 0,
    topLevelSections: 0,
    topLevelSectionsByArticle: 0,
    visitCount: 0,
  };
}

/**
 * Sums the day-bucket array into disjoint today/7-day/28-day windows.
 *
 * Bucket index 0 is "today", indices 1-7 are the past 7 days, and indices
 * 8-28 are the past 28 days (each window excludes the ones before it) —
 * mirroring the web SDK's `sumDayMetrics` (durations 1, 8, 29).
 */
function summarize(buckets: DayBucket[]): {
  today: WindowSum;
  sevenDay: WindowSum;
  twentyEightDay: WindowSum;
  daysSinceLastVisit: number;
} {
  const boundaries = [1, 8, 29];
  const windows: WindowSum[] = [];
  let running = emptyWindowSum();
  let sections = new Set<string>();
  let articleSections = new Set<string>();
  let daysSinceLastVisit = 0;

  for (let i = 0; i < Math.min(buckets.length, 29); i += 1) {
    const bucket = buckets[i]!;
    bucket.topLevelSections.forEach((s) => sections.add(s));
    bucket.topLevelSectionsByArticle.forEach((s) => articleSections.add(s));

    running = {
      pageViews: running.pageViews + bucket.pageViews,
      pageViewsByArticle: running.pageViewsByArticle + bucket.pageViewsByArticle,
      pageViewsByArticleWithPaywall: running.pageViewsByArticleWithPaywall + bucket.pageViewsByArticleWithPaywall,
      pageViewsByArticleWithRegwall: running.pageViewsByArticleWithRegwall + bucket.pageViewsByArticleWithRegwall,
      topLevelSections: sections.size,
      topLevelSectionsByArticle: articleSections.size,
      visitCount: running.visitCount + (bucket.pageViews > 0 ? 1 : 0),
    };

    if (bucket.pageViews > 0 && i > 0 && daysSinceLastVisit === 0) {
      daysSinceLastVisit = i;
    }

    if (boundaries.includes(i + 1)) {
      windows.push(running);
      running = emptyWindowSum();
      sections = new Set();
      articleSections = new Set();
    }
  }
  while (windows.length < boundaries.length) {
    windows.push(running);
  }

  return {
    today: windows[0]!,
    sevenDay: windows[1]!,
    twentyEightDay: windows[2]!,
    daysSinceLastVisit,
  };
}

/**
 * Realistic `UserDimensionRepository`: tracks real engagement in a rolling
 * day-bucket history instead of returning static values.
 *
 * Usage:
 * ```ts
 * const store = new InMemoryKeyValueStore(); // swap for AsyncStorage/MMKV in your app
 * const userDimensionRepository = new RollingUserDimensionRepository(store);
 * await userDimensionRepository.trackPageView(true, 'sports');
 * const dimensions = await userDimensionRepository.getAll();
 * ```
 */
export class RollingUserDimensionRepository implements UserDimensionRepository {
  constructor(private store: KeyValueStore) {}

  /**
   * Synchronous per the `UserDimensionRepository` interface — cache the
   * latest snapshot with `refresh()` before the SDK calls `getAll()`.
   */
  private cached: UserDimensions | null = null;

  /** Recomputes and caches the current dimensions. Call before `getAll()`, e.g. on app foreground. */
  async refresh(): Promise<UserDimensions> {
    const buckets = await this.rollForNewDay();
    const { today, sevenDay, twentyEightDay, daysSinceLastVisit } = summarize(buckets);
    const visitorType = ((await this.store.get(VISITOR_TYPE_KEY)) as VisitorType | null) ?? 'anonymous';

    this.cached = {
      todayPageViews: today.pageViews,
      todayPageViewsByArticle: today.pageViewsByArticle,
      todayPageViewsByArticleWithPaywall: today.pageViewsByArticleWithPaywall,
      todayPageViewsByArticleWithRegwall: today.pageViewsByArticleWithRegwall,
      todayTopLevelSections: today.topLevelSections,
      todayTopLevelSectionsByArticle: today.topLevelSectionsByArticle,

      sevenDayPageViews: sevenDay.pageViews,
      sevenDayPageViewsByArticle: sevenDay.pageViewsByArticle,
      sevenDayPageViewsByArticleWithPaywall: sevenDay.pageViewsByArticleWithPaywall,
      sevenDayPageViewsByArticleWithRegwall: sevenDay.pageViewsByArticleWithRegwall,
      sevenDayTopLevelSections: sevenDay.topLevelSections,
      sevenDayTopLevelSectionsByArticle: sevenDay.topLevelSectionsByArticle,
      sevenDayVisitCount: sevenDay.visitCount,

      twentyEightDayPageViews: twentyEightDay.pageViews,
      twentyEightDayPageViewsByArticle: twentyEightDay.pageViewsByArticle,
      twentyEightDayPageViewsByArticleWithPaywall: twentyEightDay.pageViewsByArticleWithPaywall,
      twentyEightDayPageViewsByArticleWithRegwall: twentyEightDay.pageViewsByArticleWithRegwall,
      twentyEightDayTopLevelSections: twentyEightDay.topLevelSections,
      twentyEightDayTopLevelSectionsByArticle: twentyEightDay.topLevelSectionsByArticle,
      twentyEightDayVisitCount: twentyEightDay.visitCount,

      daysSinceLastVisit,
      visitorType,
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      pageReferrer: null,
      sessionReferrer: null,
    };

    return this.cached;
  }

  getAll(): UserDimensions {
    if (!this.cached) {
      throw new Error('RollingUserDimensionRepository.refresh() must be awaited at least once before getAll()');
    }
    return this.cached;
  }

  /** Flips the visitor between anonymous and registered — wire to a Sign In/Sign Out control. */
  async setVisitorType(visitorType: VisitorType): Promise<void> {
    await this.store.set(VISITOR_TYPE_KEY, visitorType);
  }

  /** Records a page view in today's bucket. Call this whenever the user views a page. */
  async trackPageView(isArticle: boolean, section?: string): Promise<void> {
    await this.updateToday((bucket) => ({
      ...bucket,
      pageViews: bucket.pageViews + 1,
      pageViewsByArticle: bucket.pageViewsByArticle + (isArticle ? 1 : 0),
      topLevelSections: addUnique(bucket.topLevelSections, section),
      topLevelSectionsByArticle: isArticle ? addUnique(bucket.topLevelSectionsByArticle, section) : bucket.topLevelSectionsByArticle,
    }));
  }

  /** Records a paywall/regwall impression in today's bucket. */
  async trackWallView(wallType: 'paywall' | 'regwall', section?: string): Promise<void> {
    await this.updateToday((bucket) => ({
      ...bucket,
      pageViewsByArticleWithPaywall: bucket.pageViewsByArticleWithPaywall + (wallType === 'paywall' ? 1 : 0),
      pageViewsByArticleWithRegwall: bucket.pageViewsByArticleWithRegwall + (wallType === 'regwall' ? 1 : 0),
      topLevelSectionsByArticle: addUnique(bucket.topLevelSectionsByArticle, section),
    }));
  }

  private async updateToday(mutate: (bucket: DayBucket) => DayBucket): Promise<void> {
    const buckets = await this.rollForNewDay();
    const today = mutate(buckets[0] ?? emptyDayBucket());
    await this.store.set(DAY_BUCKETS_KEY, JSON.stringify([today, ...buckets.slice(1)]));
    await this.store.set(LAST_SEEN_DATE_KEY, todayDateString());
  }

  /** Rolls the stored bucket history forward by however many calendar days have elapsed. */
  private async rollForNewDay(): Promise<DayBucket[]> {
    const stored = await this.readBuckets();
    const lastSeen = (await this.store.get(LAST_SEEN_DATE_KEY)) ?? '';
    const daysElapsed = daysBetween(lastSeen);
    if (daysElapsed <= 0) return stored;

    const rolled = [emptyDayBucket(), ...Array(Math.max(0, daysElapsed - 1)).fill(null).map(emptyDayBucket), ...stored];
    return rolled.slice(0, MAX_STORED_DAYS);
  }

  private async readBuckets(): Promise<DayBucket[]> {
    const raw = await this.store.get(DAY_BUCKETS_KEY);
    if (!raw) return [emptyDayBucket()];
    try {
      const parsed = JSON.parse(raw) as DayBucket[];
      return parsed.length > 0 ? parsed : [emptyDayBucket()];
    } catch {
      return [emptyDayBucket()];
    }
  }
}

function addUnique(list: string[], value: string | undefined): string[] {
  if (!value || list.includes(value)) return list;
  return [...list, value];
}

/**
 * Minimal `DeviceDimensionRepository` companion for the reference above.
 * Device dimensions don't need history — they're read fresh each time.
 */
export class ReferenceDeviceDimensionRepository implements DeviceDimensionRepository {
  constructor(private appVersion: string, private platform: 'ios' | 'android') {}

  getAll(): DeviceDimensions {
    return {
      hourOfDay: new Date().getHours(),
      os: this.platform,
      viewer: `app-${this.platform}-${this.appVersion}`,
    };
  }
}
