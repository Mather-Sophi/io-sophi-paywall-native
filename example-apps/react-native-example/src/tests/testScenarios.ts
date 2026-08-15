import type {
  DeviceDimensions,
  PaywallDeciderAdapter,
  UserDimensions,
  WallDecision,
  WallDecisionOutcome,
} from '@mather-sophi/paywall';
import { ContextCodes } from './codes';

export function runScenarioValidations(
  scenario: Scenario,
  userDimension: UserDimensions,
  deviceDimension: DeviceDimensions,
  decision: WallDecision | null
): string[] {
  const errors: string[] = [];
  let test = new TestScenarioValidator(
    scenario,
    userDimension,
    deviceDimension
  );
  try {
    test.validate(decision);
  } catch (error) {
    errors.push((error as Error).message);
  }

  return errors;
}

export type Scenario = {
  name: string;
  userDimension: any;
  deviceDimension: any;
  contentId: string | null;
  decider?: PaywallDeciderAdapter;
  expected?: {
    outcome?: WallDecisionOutcome | null;
    contextCodes?: string[];
    inputCodes?: string[];
  };
};

export class TestScenarioValidator {
  name: string;
  userDimension: UserDimensions;
  deviceDimension: DeviceDimensions;
  decider?: PaywallDeciderAdapter;
  expected?: {
    outcome?: WallDecisionOutcome | null;
    contextCodes?: string[];
    inputCodes?: string[];
  };

  constructor(
    scenario: Scenario,
    userDimension: UserDimensions,
    deviceDimension: DeviceDimensions
  ) {
    this.userDimension = userDimension;
    this.deviceDimension = deviceDimension;
    this.name = scenario.name;
    this.decider = scenario.decider;
    this.expected = scenario.expected;
  }

  validate(decision: WallDecision | null): void {
    // Test 1: The decision should always be present.
    assertThat(!!decision, 'Decision should not be null');

    // Test 2: The decision should have an outcome.
    assertThat(!!decision?.outcome, 'Decision outcome should be present');

    // Test 3: The decision should have a context string.
    assertThat(
      typeof decision?.context === 'string',
      'Decision context should be a string'
    );
    assertThat(
      (decision?.context.length ?? 0) > 3,
      'Decision context cannot be empty'
    );

    // Test 4: The decision should have an inputs string.
    assertThat(
      typeof decision?.inputs === 'string',
      'Decision inputs should be a string'
    );
    assertThat(
      (decision?.inputs?.length ?? 0) > 0,
      'Decision inputs cannot be empty'
    );

    // Test 5: Compare the context against the dimension values.
    for (const [key, value] of Object.entries(this.userDimension)) {
      let contextCodeKey = ContextCodes[key as keyof typeof ContextCodes];
      if (contextCodeKey) {
        const expectedCode = `${contextCodeKey}${String(value).padStart(2, '0')}`;
        assertContains(decision?.context ?? '', expectedCode, key);
      }
    }

    // Test 6: Compare the inputs against the dimension values.
    for (const [key, value] of Object.entries(this.deviceDimension)) {
      let inputCodeKey = ContextCodes[key as keyof typeof ContextCodes];
      if (inputCodeKey) {
        const expectedCode = `${inputCodeKey}:${String(value).padStart(2, '0')}`;
        assertContains(decision?.inputs ?? '', expectedCode, key);
      }
    }

    // Test 7: Test OS input code.
    const hasOsCode =
      decision?.inputs?.includes('EA:ios') ||
      decision?.inputs?.includes('EA:android') ||
      false;
    assertThat(
      hasOsCode,
      'inputs should contain OS code (EA:ios or EA:android)'
    );

    // Test 8: If expected outcome is provided, compare it with the actual outcome.
    if (this.expected?.outcome) {
      const expectedOutcome = this.expected.outcome;
      const actualOutcome = decision?.outcome;

      for (const [key, value] of Object.entries(expectedOutcome)) {
        const actualValue = (actualOutcome as Record<string, unknown>)[key];
        assertThat(
          actualValue === value,
          `Outcome mismatch for ${key}. Expected: ${String(value)}, Actual: ${String(actualValue)}`
        );
      }
    }

    // Test 9: Validate trace codes.
    const trace = decision?.trace ?? '';
    assertThat(trace.length > 0, 'Decision trace cannot be empty');
    if (this.userDimension.visitorType === 'anonymous') {
      this.traceValidations(trace, ['t', 'b', 'd'], ['t']);
    } else {
      this.traceValidations(trace, ['t', 'd'], ['t']);
    }

    // Test 10: Validate expected context and input codes if provided.
    if (this.expected?.contextCodes) {
      for (const contextCode of this.expected.contextCodes) {
        assertContains(decision?.context ?? '', contextCode, 'context');
      }
    }

    if (this.expected?.inputCodes) {
      for (const inputCode of this.expected.inputCodes) {
        assertContains(decision?.inputs ?? '', inputCode, 'inputs');
      }
    }
  }

  traceValidations(
    trace: string,
    allowedPrefixes: string[],
    requiredPrefixes: string[]
  ) {
    const rawGroups = trace.match(/[a-z][0-9x-]*/g) ?? [];
    const tokens = trace.match(/[a-z][0-9x-]{6}/g) ?? [];
    const reconstructed = tokens.join('');

    assertThat(
      trace === trace.toLowerCase(),
      `Trace should be lowercase, got: ${trace}`
    );

    rawGroups.forEach((group) => {
      assertThat(
        group.length <= 7,
        `Trace group '${group}' should not be more than 7 characters`
      );
    });

    assertThat(
      tokens.length > 0,
      'Trace should contain at least one trace code token'
    );
    assertThat(
      reconstructed === trace,
      `Trace should be composed of [prefix][6 chars] tokens, got: ${trace}`
    );

    tokens.forEach((token) => {
      const prefix = token.charAt(0);
      assertThat(
        allowedPrefixes.includes(prefix),
        `Trace contains disallowed prefix '${prefix}' in token '${token}'. Allowed prefixes: ${allowedPrefixes.join(', ')}`
      );
    });

    requiredPrefixes.forEach((requiredPrefix) => {
      const hasRequiredPrefix = tokens.some(
        (token) => token.charAt(0) === requiredPrefix
      );
      assertThat(
        hasRequiredPrefix,
        `Trace should contain at least one '${requiredPrefix}xxxxxx' token, got: ${trace}`
      );
    });
  }
}

export function assertThat(condition: boolean, message: string): void {
  if (!condition) {
    throw new Error(message);
  }
}

export function assertContains(
  haystack: string,
  needle: string,
  label: string
): void {
  assertThat(
    haystack.includes(needle),
    `${label} missing substring: ${needle}`
  );
}

export function generateTestScenarios(): Scenario[] {
  return [
    {
      name: 'Basic Test: Standard Page Views',
      contentId: 'test-content-basic',
      userDimension: {
        todayPageViews: 5,
        todayPageViewsByArticle: 3,
        todayPageViewsByArticleWithPaywall: 1,
        todayPageViewsByArticleWithRegwall: 0,
        todayTopLevelSections: 2,
        todayTopLevelSectionsByArticle: 1,
        sevenDayPageViews: 5,
        sevenDayPageViewsByArticle: 3,
        sevenDayPageViewsByArticleWithPaywall: 2,
        sevenDayPageViewsByArticleWithRegwall: 1,
        sevenDayTopLevelSections: 2,
        sevenDayTopLevelSectionsByArticle: 1,
        sevenDayVisitCount: 2,
        twentyEightDayPageViews: 10,
        twentyEightDayPageViewsByArticle: 8,
        twentyEightDayPageViewsByArticleWithPaywall: 3,
        twentyEightDayPageViewsByArticleWithRegwall: 1,
        twentyEightDayTopLevelSections: 4,
        twentyEightDayTopLevelSectionsByArticle: 3,
        twentyEightDayVisitCount: 4,
        daysSinceLastVisit: 0,
        visitorType: 'registered',
        timezone: 'America/New_York',
        pageReferrer: 'https://example.com',
        sessionReferrer: 'https://example.com/session',
      },
      deviceDimension: {},
      expected: {
        contextCodes: [
          'AA05', 'AB03', 'AC01', 'AF00', 'AD02', 'AE01',
          'BA05', 'BB03', 'BC02', 'BG01', 'BD02', 'BE01', 'BF02',
          'CA10', 'CB08', 'CC03', 'CG01', 'CD04', 'CE03', 'CF04',
          'DA00',
        ],
        inputCodes: ['FA:01', 'DE:05', 'DB:America/New_York', 'EB:06'],
      },
    },
    {
      name: 'Partial: Minimal Page Views (Today only)',
      contentId: 'test-content-minimal',
      userDimension: {
        todayPageViews: 1,
        daysSinceLastVisit: 0,
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA01', 'DA00'],
        inputCodes: [],
      },
    },
    {
      name: 'Partial: Today + 7 Day metrics only',
      contentId: 'test-content-7day',
      userDimension: {
        todayPageViews: 5,
        todayPageViewsByArticle: 3,
        sevenDayPageViews: 5,
        sevenDayPageViewsByArticle: 15,
        daysSinceLastVisit: 2,
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA05', 'AB03', 'BA05', 'DA02'],
        inputCodes: [],
      },
    },
    {
      name: 'Partial: Zero-filled dimensions',
      contentId: 'test-content-zero',
      userDimension: {
        todayPageViews: 0,
        todayPageViewsByArticle: 0,
        sevenDayPageViews: 0,
        twentyEightDayPageViews: 0,
        daysSinceLastVisit: 30,
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA00', 'AB00', 'BA00', 'CA00', 'DA30'],
        inputCodes: [],
      },
    },
    {
      name: 'New User: Anonymous visitor with no prior visits',
      contentId: 'test-content-anon',
      userDimension: {
        todayPageViews: 1,
        daysSinceLastVisit: 365,
        visitorType: 'anonymous',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA01', 'DA365'],
        inputCodes: [],
      },
    },
    {
      name: 'Registered User: Multiple visits with full metrics',
      contentId: 'test-content-registered',
      userDimension: {
        todayPageViews: 10,
        todayPageViewsByArticle: 8,
        todayPageViewsByArticleWithPaywall: 3,
        todayTopLevelSections: 5,
        todayTopLevelSectionsByArticle: 4,
        sevenDayPageViews: 45,
        sevenDayPageViewsByArticle: 35,
        sevenDayPageViewsByArticleWithPaywall: 12,
        sevenDayTopLevelSections: 10,
        sevenDayTopLevelSectionsByArticle: 8,
        sevenDayVisitCount: 7,
        twentyEightDayPageViews: 120,
        twentyEightDayPageViewsByArticle: 90,
        twentyEightDayPageViewsByArticleWithPaywall: 40,
        twentyEightDayTopLevelSections: 25,
        twentyEightDayTopLevelSectionsByArticle: 20,
        twentyEightDayVisitCount: 20,
        daysSinceLastVisit: 0,
        visitorType: 'registered',
        timezone: 'America/New_York',
      },
      deviceDimension: {},
      expected: {
        contextCodes: [
          'AA10', 'AB08', 'AC03', 'AD05', 'AE04',
          'BA45', 'BB35', 'BC12', 'BD10', 'BE08', 'BF07',
          'CA120', 'CB90', 'CC40', 'CD25', 'CE20', 'CF20',
          'DA00',
        ],
        inputCodes: [],
      },
    },
    {
      name: 'OnDevice Decision: Null contentId - anonymous user',
      contentId: null,
      userDimension: {
        sevenDayPageViews: 5,
        sevenDayPageViewsByArticle: 3,
        sevenDayPageViewsByArticleWithPaywall: 1,
        daysSinceLastVisit: 2,
        visitorType: 'anonymous',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['BA05', 'BB03', 'BC01', 'DA02'],
        inputCodes: [],
      },
    },
    {
      name: 'OnDevice Decision: Null contentId - registered user',
      contentId: null,
      userDimension: {
        sevenDayPageViews: 5,
        sevenDayPageViewsByArticle: 3,
        sevenDayPageViewsByArticleWithPaywall: 1,
        daysSinceLastVisit: 2,
        visitorType: 'registered',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['BA05', 'BB03', 'BC01', 'DA02'],
        inputCodes: [],
      },
    },
    {
      name: 'PageReferrer: Direct traffic (empty referrer)',
      contentId: 'test-content-direct',
      userDimension: {
        todayPageViews: 2,
        daysSinceLastVisit: 0,
        pageReferrer: '',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA02', 'DA00'],
        inputCodes: ['DE:01'],
      },
    },
    {
      name: 'PageReferrer: Internal traffic (same domain)',
      contentId: 'test-content-internal',
      userDimension: {
        todayPageViews: 3,
        daysSinceLastVisit: 0,
        pageReferrer: 'https://test.sophi.codes/article/previous-story',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA03', 'DA00'],
        inputCodes: ['DE:02'],
      },
    },
    {
      name: 'PageReferrer: Search engine traffic (Google)',
      contentId: 'test-content-search-google',
      userDimension: {
        todayPageViews: 4,
        daysSinceLastVisit: 0,
        pageReferrer: 'https://www.google.com/search?q=news',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA04', 'DA00'],
        inputCodes: ['DE:03', 'DF:00'],
      },
    },
    {
      name: 'PageReferrer: Social media traffic (Facebook)',
      contentId: 'test-content-social-fb',
      userDimension: {
        todayPageViews: 5,
        daysSinceLastVisit: 0,
        pageReferrer: 'https://www.facebook.com/page/12345',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA05', 'DA00'],
        inputCodes: ['DE:04', 'DF:04'],
      },
    },
    {
      name: 'PageReferrer: Other referrer source (unknown domain)',
      contentId: 'test-content-other-source',
      userDimension: {
        todayPageViews: 3,
        daysSinceLastVisit: 0,
        pageReferrer: 'https://random-blog.com/article',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA03', 'DA00'],
        inputCodes: ['DE:05'],
      },
    },
    {
      name: 'SessionReferrer: Campaign traffic with utm_campaign',
      contentId: 'test-content-campaign',
      userDimension: {
        todayPageViews: 2,
        daysSinceLastVisit: 0,
        sessionReferrer:
          'https://test.sophi.codes/landing?utm_campaign=summer_sale',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA02', 'DA00'],
        inputCodes: ['GC:campaign', 'GA:summer_sale'],
      },
    },
    {
      name: 'SessionReferrer: Direct session',
      contentId: 'test-content-session-direct',
      userDimension: {
        todayPageViews: 2,
        daysSinceLastVisit: 1,
        sessionReferrer: '',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA02', 'DA01'],
        inputCodes: ['GC:direct'],
      },
    },
    {
      name: 'SessionReferrer: Internal session (same domain)',
      contentId: 'test-content-session-internal',
      userDimension: {
        todayPageViews: 2,
        daysSinceLastVisit: 0,
        sessionReferrer: 'https://blog.test.sophi.codes/old-article',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA02', 'DA00'],
        inputCodes: ['GC:internal'],
      },
    },
    {
      name: 'SessionReferrer: Search engine session (Bing)',
      contentId: 'test-content-session-search-bing',
      userDimension: {
        todayPageViews: 3,
        daysSinceLastVisit: 0,
        sessionReferrer: 'https://www.bing.com/search?q=technology',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA03', 'DA00'],
        inputCodes: ['GC:search', 'GD:bing'],
      },
    },
    {
      name: 'SessionReferrer: Social session (Instagram)',
      contentId: 'test-content-session-social-ig',
      userDimension: {
        todayPageViews: 4,
        daysSinceLastVisit: 0,
        sessionReferrer: 'https://www.instagram.com/profile/story123',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA04', 'DA00'],
        inputCodes: ['GC:social', 'GD:instagram'],
      },
    },
    {
      name: 'SessionReferrer: Search with channel (Google News)',
      contentId: 'test-content-session-google-news',
      userDimension: {
        todayPageViews: 2,
        daysSinceLastVisit: 0,
        sessionReferrer: 'https://news.google.com/stories/article123',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA02', 'DA00'],
        inputCodes: ['GC:search', 'GD:google', 'GE:news'],
      },
    },
    {
      name: 'Referrer with www variations',
      contentId: 'test-content-www-variations',
      userDimension: {
        todayPageViews: 2,
        daysSinceLastVisit: 0,
        pageReferrer: 'https://www.sophi.codes/page',
        sessionReferrer: 'https://test.sophi.codes/landing',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA02', 'DA00'],
        inputCodes: ['DE:05', 'GC:internal'],
      },
    },
    {
      name: 'Content Properties: Custom user and content properties',
      contentId: 'test-content-properties',
      userDimension: {
        todayPageViews: 3,
        daysSinceLastVisit: 0,
        visitorType: 'registered',
      },
      deviceDimension: {},
      expected: {
        contextCodes: ['AA03', 'DA00'],
        inputCodes: [],
      },
    },
  ];
}
