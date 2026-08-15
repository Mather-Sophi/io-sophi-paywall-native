import { Platform } from 'react-native';
import {
  type DeviceDimensionRepository,
  type DeviceDimensions,
  type UserDimensionRepository,
  type UserDimensions,
} from '@mather-sophi/paywall';

const baseUserDimensions: UserDimensions = {
  todayPageViews: 5,
  todayPageViewsByArticle: 3,
  todayPageViewsByArticleWithPaywall: 1,
  todayPageViewsByArticleWithRegwall: 0,
  todayTopLevelSections: 2,
  todayTopLevelSectionsByArticle: 1,
  sevenDayPageViews: 25,
  sevenDayPageViewsByArticle: 15,
  sevenDayPageViewsByArticleWithPaywall: 5,
  sevenDayPageViewsByArticleWithRegwall: 2,
  sevenDayTopLevelSections: 8,
  sevenDayTopLevelSectionsByArticle: 6,
  sevenDayVisitCount: 5,
  twentyEightDayPageViews: 100,
  twentyEightDayPageViewsByArticle: 60,
  twentyEightDayPageViewsByArticleWithPaywall: 20,
  twentyEightDayPageViewsByArticleWithRegwall: 10,
  twentyEightDayTopLevelSections: 15,
  twentyEightDayTopLevelSectionsByArticle: 12,
  twentyEightDayVisitCount: 18,
  daysSinceLastVisit: 0,
  visitorType: 'registered',
  timezone: 'America/New_York',
  pageReferrer: null,
  sessionReferrer: null,
};

const baseDeviceDimensions: DeviceDimensions = {
  hourOfDay: new Date().getHours(),
  os: Platform.OS === 'ios' ? 'ios' : 'android',
  viewer: 'paywall-kit-example-1.0.0',
};

export class MockUserDimensionRepository implements UserDimensionRepository {
  private dimensions: UserDimensions = { ...baseUserDimensions };

  getAll(): UserDimensions {
    return this.dimensions;
  }

  reset(): void {
    this.dimensions = { ...baseUserDimensions };
  }

  update(dimensions: Partial<UserDimensions>): void {
    this.dimensions = { ...this.dimensions, ...dimensions };
  }
}

export class MockDeviceDimensionRepository
  implements DeviceDimensionRepository
{
  private dimensions: DeviceDimensions = { ...baseDeviceDimensions };

  getAll(): DeviceDimensions {
    return this.dimensions;
  }

  reset(): void {
    this.dimensions = { ...baseDeviceDimensions };
  }

  update(dimensions: Partial<DeviceDimensions>): void {
    this.dimensions = { ...this.dimensions, ...dimensions };
  }
}
