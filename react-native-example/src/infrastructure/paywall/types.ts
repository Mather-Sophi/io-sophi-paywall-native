/**
 * Type definitions for Sophi Paywall user and device dimensions.
 *
 * These types mirror the currently supported repository contracts used by
 * the native paywall SDK wrappers.
 */

export type VisitorType = 'anonymous' | 'registered';
export type DeviceOs = 'ios' | 'android';

export interface UserDimensions {
  // 1-day metrics
  todayPageViews: number;
  todayPageViewsByArticle: number;
  todayPageViewsByArticleWithPaywall: number;
  todayPageViewsByArticleWithRegwall: number;
  todayTopLevelSections: number;
  todayTopLevelSectionsByArticle: number;

  // 7-day metrics
  sevenDayPageViews: number;
  sevenDayPageViewsByArticle: number;
  sevenDayPageViewsByArticleWithPaywall: number;
  sevenDayPageViewsByArticleWithRegwall: number;
  sevenDayTopLevelSections: number;
  sevenDayTopLevelSectionsByArticle: number;
  sevenDayVisitCount: number;

  // 28-day metrics
  twentyEightDayPageViews: number;
  twentyEightDayPageViewsByArticle: number;
  twentyEightDayPageViewsByArticleWithPaywall: number;
  twentyEightDayPageViewsByArticleWithRegwall: number;
  twentyEightDayTopLevelSections: number;
  twentyEightDayTopLevelSectionsByArticle: number;
  twentyEightDayVisitCount: number;

  // Session/context fields
  daysSinceLastVisit: number;
  visitorType: VisitorType;
  timezone: string;
  pageReferrer?: string | null;
  sessionReferrer?: string | null;
}

export interface DeviceDimensions {
  hourOfDay: number;
  os: DeviceOs;
  viewer: string | null;
}
