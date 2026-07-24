/**
 * Type definitions for Sophi Paywall user and device dimensions.
 * 
 * These types mirror the Sophi library's dimension requirements and provide
 * extensive documentation with practical examples for each field.
 */

// ============================================================================
// ENUM TYPES
// ============================================================================

/**
 * Visitor type classification.
 * 
 * - anonymous: User is not logged in
 * - registered: User has created an account and is logged in
 */
export type VisitorType = 'anonymous' | 'registered';

/**
 * Referrer medium classification.
 * 
 * Categorizes how the user arrived at the content:
 * - campaign: Came from a marketing campaign (e.g., email newsletter, paid ads)
 * - direct: Typed URL directly or used a bookmark
 * - internal: Navigated from another page on the same site
 * - search: Came from a search engine
 * - social: Came from a social media platform
 * - other: Any other source not fitting the above categories
 */
export type ReferrerMedium = 'campaign' | 'direct' | 'internal' | 'search' | 'social' | 'other';

/**
 * Referrer source classification.
 * 
 * Identifies the specific platform or service the user came from:
 * - google: Google (search, news, discover, etc.)
 * - yahoo: Yahoo search
 * - duckduckgo: DuckDuckGo search
 * - bing: Bing search
 * - facebook: Facebook
 * - instagram: Instagram
 * - x: X (formerly Twitter)
 * - t: Alternative identifier for X/Twitter
 * - linkedin: LinkedIn
 * - reddit: Reddit
 * - newsletter: Email newsletter
 * 
 * EXAMPLES:
 * - User clicks a link in Facebook app → 'facebook'
 * - User searches on Google and clicks result → 'google'
 * - User clicks link in email newsletter → 'newsletter'
 * - User clicks Reddit post → 'reddit'
 */
export type ReferrerSource =
    | 'google'
    | 'yahoo'
    | 'duckduckgo'
    | 'bing'
    | 'facebook'
    | 'instagram'
    | 'x'
    | 't'
    | 'linkedin'
    | 'reddit'
    | 'newsletter';

/**
 * Referrer channel classification for Google traffic.
 * 
 * Distinguishes between different Google entry points:
 * - search: Google Search results (e.g., user searched "news today")
 * - news: Google News app or news.google.com
 * - discover: Google Discover feed (personalized content feed on mobile)
 * 
 * EXAMPLES:
 * - User searches "tech news" on google.com → 'search'
 * - User browses Google News app → 'news'
 * - User swipes through Google Discover feed on Android → 'discover'
 */
export type ReferrerChannel = 'search' | 'news' | 'discover';

// ============================================================================
// USER DIMENSIONS
// ============================================================================

/**
 * User-level dimensions for paywall decision-making.
 * 
 * These metrics track visitor engagement over time and are critical for
 * determining paywall strategy. All metrics are cumulative and should be
 * persisted locally on the device.
 * 
 * IMPORTANT: Metrics are segmented by time period:
 * - "today" = current day (resets at midnight)
 * - "sevenDay" = past 7 days NOT including today
 * - "twentyEightDay" = past 28 days NOT including today OR past 7 days
 */
export interface UserDimensions {
    // ========================================================================
    // 1-DAY METRICS (Today's activity)
    // ========================================================================

    /** Total number of pages viewed today (all page types) */
    todayPageViews: number;

    /** Number of article pages viewed today */
    todayPageViewsByArticle: number;

    /** Number of article pages with a paywall viewed today */
    todayPageViewsByArticleWithPaywall: number;

    /** Number of article pages with a registration wall viewed today */
    todayPageViewsByArticleWithRegwall: number;

    /** Number of unique top-level sections visited today (e.g., "sports", "politics") */
    todayTopLevelSections: number;

    /** Number of unique top-level sections for articles viewed today */
    todayTopLevelSectionsByArticle: number;

    // ========================================================================
    // 7-DAY METRICS (Past week, excluding today)
    // ========================================================================

    /** Total page views over the past 7 days (NOT including today) */
    sevenDayPageViews: number;

    /** Article page views over the past 7 days (NOT including today) */
    sevenDayPageViewsByArticle: number;

    /** Article page views with paywall over the past 7 days (NOT including today) */
    sevenDayPageViewsByArticleWithPaywall: number;

    /** Article page views with regwall over the past 7 days (NOT including today) */
    sevenDayPageViewsByArticleWithRegwall: number;

    /** Unique top-level sections visited over 7 days (NOT including today) */
    sevenDayTopLevelSections: number;

    /** Unique sections for articles over 7 days (NOT including today) */
    sevenDayTopLevelSectionsByArticle: number;

    /** Number of distinct days the user visited in the past 7 days */
    sevenDayVisitCount: number;

    // ========================================================================
    // 28-DAY METRICS (Past month, excluding today and past 7 days)
    // ========================================================================

    /** Total page views over the past 28 days (NOT including today OR past 7 days) */
    twentyEightDayPageViews: number;

    /** Article page views over the past 28 days (NOT including today OR past 7 days) */
    twentyEightDayPageViewsByArticle: number;

    /** Article page views with paywall over the past 28 days (NOT including today OR past 7 days) */
    twentyEightDayPageViewsByArticleWithPaywall: number;

    /** Article page views with regwall over the past 28 days (NOT including today OR past 7 days) */
    twentyEightDayPageViewsByArticleWithRegwall: number;

    /** Unique top-level sections visited over 28 days (NOT including today OR past 7 days) */
    twentyEightDayTopLevelSections: number;

    /** Unique sections for articles over 28 days (NOT including today OR past 7 days) */
    twentyEightDayTopLevelSectionsByArticle: number;

    /** Number of distinct days the user visited in the past 28 days (NOT including today OR past 7 days) */
    twentyEightDayVisitCount: number;

    // ========================================================================
    // CURRENT SESSION CONTEXT
    // ========================================================================

    /** Visitor type: anonymous or registered */
    visitorType: VisitorType;

    /** 
     * Referrer medium for current session.
     * 
     * DETECTION EXAMPLES:
     * - User types URL directly → 'direct'
     * - User clicks link from Facebook → 'social'
     * - User clicks Google search result → 'search'
     * - User clicks email newsletter link → 'campaign'
     * - User navigates from homepage to article → 'internal'
     */
    referrerMedium: ReferrerMedium;

    /** 
     * Referrer source for current session.
     * 
     * DETECTION EXAMPLES:
     * - Referrer contains "facebook.com" → 'facebook'
     * - Referrer contains "google.com" → 'google'
     * - Referrer contains "reddit.com" → 'reddit'
     * - Referrer contains "linkedin.com" → 'linkedin'
     * - Referrer contains "instagram.com" → 'instagram'
     * - Referrer contains "twitter.com" or "t.co" → 'x'
     * - Referrer contains campaign parameter → 'newsletter'
     */
    referrerSource?: ReferrerSource;

    /** 
     * Referrer channel (primarily for Google traffic).
     * 
     * DETECTION EXAMPLES:
     * - Referrer is "google.com" with search query → 'search'
     * - Referrer is "news.google.com" → 'news'
     * - Referrer contains "googleusercontent.com" (Discover) → 'discover'
     */
    referrerChannel?: ReferrerChannel;
}

// ============================================================================
// DEVICE DIMENSIONS
// ============================================================================

/**
 * Device-level dimensions for paywall decision-making.
 * 
 * These metrics provide context about the device and platform being used.
 */
export interface DeviceDimensions {
    /** Device type (e.g., "mobile", "tablet", "desktop") */
    deviceType: string;

    /** Operating system (e.g., "Android", "iOS", "Windows") */
    os: string;

    /** Browser or app name (e.g., "Chrome", "Safari", "NewsPublisher App") */
    browser: string;

    /** Whether the user is accessing via a native app */
    isNativeApp: boolean;
}

// ============================================================================
// REFERRER DETECTION UTILITIES
// ============================================================================

/**
 * Detects referrer information from a URL string.
 * 
 * PRODUCTION IMPLEMENTATION NOTES:
 * - In a real app, you would parse the referrer from deep link parameters or initial URL
 * - You might use UTM parameters (utm_source, utm_medium, utm_campaign)
 * - For web views, you would track the HTTP Referer header
 * 
 * EXAMPLES OF REFERRER DETECTION:
 * 
 * 1. SOCIAL MEDIA:
 *    - facebook.com → { medium: 'social', source: 'facebook', channel: undefined }
 *    - instagram.com → { medium: 'social', source: 'instagram', channel: undefined }
 *    - reddit.com → { medium: 'social', source: 'reddit', channel: undefined }
 *    - linkedin.com → { medium: 'social', source: 'linkedin', channel: undefined }
 *    - twitter.com or t.co → { medium: 'social', source: 'x', channel: undefined }
 * 
 * 2. SEARCH ENGINES:
 *    - google.com/search → { medium: 'search', source: 'google', channel: 'search' }
 *    - news.google.com → { medium: 'search', source: 'google', channel: 'news' }
 *    - googleusercontent.com (Discover) → { medium: 'search', source: 'google', channel: 'discover' }
 *    - bing.com → { medium: 'search', source: 'bing', channel: undefined }
 *    - yahoo.com → { medium: 'search', source: 'yahoo', channel: undefined }
 *    - duckduckgo.com → { medium: 'search', source: 'duckduckgo', channel: undefined }
 * 
 * 3. CAMPAIGNS:
 *    - UTM parameters present → { medium: 'campaign', source: 'newsletter', channel: undefined }
 *    - Email link with campaign ID → { medium: 'campaign', source: 'newsletter', channel: undefined }
 * 
 * 4. DIRECT:
 *    - No referrer → { medium: 'direct', source: undefined, channel: undefined }
 *    - Bookmark → { medium: 'direct', source: undefined, channel: undefined }
 * 
 * 5. INTERNAL:
 *    - Same domain → { medium: 'internal', source: undefined, channel: undefined }
 */
export function detectReferrer(
    referrerUrl?: string
): { medium: ReferrerMedium; source?: ReferrerSource; channel?: ReferrerChannel } {
    if (!referrerUrl) {
        return { medium: 'direct' };
    }

    try {
        const url = new URL(referrerUrl);
        const host = url.hostname.toLowerCase();
        const path = url.pathname.toLowerCase();
        const searchParams = url.searchParams;

        // Check for UTM parameters (campaign tracking)
        const utmSource = searchParams.get('utm_source');
        const utmMedium = searchParams.get('utm_medium');
        if (utmSource || utmMedium) {
            return { medium: 'campaign', source: 'newsletter' };
        }

        // Detect social media sources
        if (host.includes('facebook.com') || host.includes('fb.com')) {
            return { medium: 'social', source: 'facebook' };
        }
        if (host.includes('instagram.com')) {
            return { medium: 'social', source: 'instagram' };
        }
        if (host.includes('twitter.com') || host.includes('t.co')) {
            return { medium: 'social', source: 'x' };
        }
        if (host.includes('linkedin.com')) {
            return { medium: 'social', source: 'linkedin' };
        }
        if (host.includes('reddit.com')) {
            return { medium: 'social', source: 'reddit' };
        }

        // Detect search engines and channels
        if (host.includes('google.com')) {
            let channel: ReferrerChannel | undefined;
            if (host.includes('news.google.com')) {
                channel = 'news';
            } else if (path.includes('/search')) {
                channel = 'search';
            }
            return { medium: 'search', source: 'google', channel };
        }
        if (host.includes('googleusercontent.com')) {
            return { medium: 'search', source: 'google', channel: 'discover' };
        }
        if (host.includes('bing.com')) {
            return { medium: 'search', source: 'bing' };
        }
        if (host.includes('yahoo.com')) {
            return { medium: 'search', source: 'yahoo' };
        }
        if (host.includes('duckduckgo.com')) {
            return { medium: 'search', source: 'duckduckgo' };
        }

        // Check if internal (same domain)
        if (host.includes('publisher.news') || host.includes('news.publisher')) {
            return { medium: 'internal' };
        }

        // Default to OTHER for unknown sources
        return { medium: 'other' };
    } catch (error) {
        console.warn('Failed to parse referrer URL:', error);
        return { medium: 'direct' };
    }
}
