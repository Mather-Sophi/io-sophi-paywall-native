// Prefixes for encoded context and input codes
export const ContextCodes = {
  todayPageViews: 'AA',
  todayPageViewsByArticle: 'AB',
  todayPageViewsByArticleWithPaywall: 'AC',
  todayTopLevelSections: 'AD',
  todayTopLevelSectionsByArticle: 'AE',
  todayPageViewsByArticleWithRegwall: 'AF',

  sevenDayPageViews: 'BA',
  sevenDayPageViewsByArticle: 'BB',
  sevenDayPageViewsByArticleWithPaywall: 'BC',
  sevenDayTopLevelSections: 'BD',
  sevenDayTopLevelSectionsByArticle: 'BE',
  sevenDayVisitCount: 'BF',
  sevenDayPageViewsByArticleWithRegwall: 'BG',

  twentyEightDayPageViews: 'CA',
  twentyEightDayPageViewsByArticle: 'CB',
  twentyEightDayPageViewsByArticleWithPaywall: 'CC',
  twentyEightDayTopLevelSections: 'CD',
  twentyEightDayTopLevelSectionsByArticle: 'CE',
  twentyEightDayVisitCount: 'CF',
  twentyEightDayPageViewsByArticleWithRegwall: 'CG',

  daysSinceLastVisit: 'DA',
} as const;

export const InputCodes = {
  // Visitor and page referrer
  visitorType: 'FA',
  referrerMedium: 'DE',
  referrerSource: 'DF',
  referrerChannel: 'DG',

  // Device
  os: 'EA',
  type: 'EB',
  viewer: 'EC',

  // Timezone and hour
  hourOfDay: 'DD',
  timeZone: 'DB',

  // Session referrer
  sessionCampaignName: 'GA',
  sessionReferrerDomain: 'GB',
  sessionMedium: 'GC',
  sessionSource: 'GD',
  sessionChannel: 'GE',
} as const;
