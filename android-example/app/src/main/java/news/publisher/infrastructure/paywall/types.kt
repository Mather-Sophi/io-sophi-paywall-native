package news.publisher.infrastructure.paywall

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
enum class VisitorType(val value: String) {
    ANONYMOUS("anonymous"),
    REGISTERED("registered")
}

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
enum class ReferrerMedium(val value: String) {
    CAMPAIGN("campaign"),
    DIRECT("direct"),
    INTERNAL("internal"),
    SEARCH("search"),
    SOCIAL("social"),
    OTHER("other")
}

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
 * - User clicks a link in Facebook app → facebook
 * - User searches on Google and clicks result → google
 * - User clicks link in email newsletter → newsletter
 * - User clicks Reddit post → reddit
 */
enum class ReferrerSource(val value: String) {
    GOOGLE("google"),
    YAHOO("yahoo"),
    DUCKDUCKGO("duckduckgo"),
    BING("bing"),
    FACEBOOK("facebook"),
    INSTAGRAM("instagram"),
    X("x"),
    T("t"),
    LINKEDIN("linkedin"),
    REDDIT("reddit"),
    NEWSLETTER("newsletter")
}

/**
 * Referrer channel classification for Google traffic.
 * 
 * Distinguishes between different Google entry points:
 * - search: Google Search results (e.g., user searched "news today")
 * - news: Google News app or news.google.com
 * - discover: Google Discover feed (personalized content feed on mobile)
 * 
 * EXAMPLES:
 * - User searches "tech news" on google.com → search
 * - User browses Google News app → news
 * - User swipes through Google Discover feed on Android → discover
 */
enum class ReferrerChannel(val value: String) {
    SEARCH("search"),
    NEWS("news"),
    DISCOVER("discover")
}

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
data class UserDimensions(
    // ========================================================================
    // 1-DAY METRICS (Today's activity)
    // ========================================================================
    
    /** Total number of pages viewed today (all page types) */
    val todayPageViews: Int,
    
    /** Number of article pages viewed today */
    val todayPageViewsByArticle: Int,
    
    /** Number of article pages with a paywall viewed today */
    val todayPageViewsByArticleWithPaywall: Int,
    
    /** Number of article pages with a registration wall viewed today */
    val todayPageViewsByArticleWithRegwall: Int,
    
    /** Number of unique top-level sections visited today (e.g., "sports", "politics") */
    val todayTopLevelSections: Int,
    
    /** Number of unique top-level sections for articles viewed today */
    val todayTopLevelSectionsByArticle: Int,
    
    // ========================================================================
    // 7-DAY METRICS (Past week, excluding today)
    // ========================================================================
    
    /** Total page views over the past 7 days (NOT including today) */
    val sevenDayPageViews: Int,
    
    /** Article page views over the past 7 days (NOT including today) */
    val sevenDayPageViewsByArticle: Int,
    
    /** Article page views with paywall over the past 7 days (NOT including today) */
    val sevenDayPageViewsByArticleWithPaywall: Int,
    
    /** Article page views with regwall over the past 7 days (NOT including today) */
    val sevenDayPageViewsByArticleWithRegwall: Int,
    
    /** Unique top-level sections visited over 7 days (NOT including today) */
    val sevenDayTopLevelSections: Int,
    
    /** Unique sections for articles over 7 days (NOT including today) */
    val sevenDayTopLevelSectionsByArticle: Int,
    
    /** Number of distinct days the user visited in the past 7 days */
    val sevenDayVisitCount: Int,
    
    // ========================================================================
    // 28-DAY METRICS (Past month, excluding today and past 7 days)
    // ========================================================================
    
    /** Total page views over the past 28 days (NOT including today OR past 7 days) */
    val twentyEightDayPageViews: Int,
    
    /** Article page views over the past 28 days (NOT including today OR past 7 days) */
    val twentyEightDayPageViewsByArticle: Int,
    
    /** Article page views with paywall over the past 28 days (NOT including today OR past 7 days) */
    val twentyEightDayPageViewsByArticleWithPaywall: Int,
    
    /** Article page views with regwall over the past 28 days (NOT including today OR past 7 days) */
    val twentyEightDayPageViewsByArticleWithRegwall: Int,
    
    /** Unique top-level sections visited over 28 days (NOT including today OR past 7 days) */
    val twentyEightDayTopLevelSections: Int,
    
    /** Unique sections for articles over 28 days (NOT including today OR past 7 days) */
    val twentyEightDayTopLevelSectionsByArticle: Int,
    
    /** Number of distinct days the user visited in the past 28 days (NOT including today OR past 7 days) */
    val twentyEightDayVisitCount: Int,
    
    // ========================================================================
    // CURRENT SESSION CONTEXT
    // ========================================================================
    
    /** Visitor type: anonymous or registered */
    val visitorType: VisitorType,
    
    /** 
     * Referrer medium for current session.
     * 
     * DETECTION EXAMPLES:
     * - User types URL directly → DIRECT
     * - User clicks link from Facebook → SOCIAL
     * - User clicks Google search result → SEARCH
     * - User clicks email newsletter link → CAMPAIGN
     * - User navigates from homepage to article → INTERNAL
     */
    val referrerMedium: ReferrerMedium,
    
    /** 
     * Referrer source for current session.
     * 
     * DETECTION EXAMPLES:
     * - Referrer contains "facebook.com" → FACEBOOK
     * - Referrer contains "google.com" → GOOGLE
     * - Referrer contains "reddit.com" → REDDIT
     * - Referrer contains "linkedin.com" → LINKEDIN
     * - Referrer contains "instagram.com" → INSTAGRAM
     * - Referrer contains "twitter.com" or "t.co" → X
     * - Referrer contains campaign parameter → NEWSLETTER
     */
    val referrerSource: ReferrerSource?,
    
    /** 
     * Referrer channel (primarily for Google traffic).
     * 
     * DETECTION EXAMPLES:
     * - Referrer is "google.com" with search query → SEARCH
     * - Referrer is "news.google.com" → NEWS
     * - Referrer contains "googleusercontent.com" (Discover) → DISCOVER
     */
    val referrerChannel: ReferrerChannel?
)

// ============================================================================
// DEVICE DIMENSIONS
// ============================================================================

/**
 * Device-level dimensions for paywall decision-making.
 * 
 * These metrics provide context about the device and platform being used.
 */
data class DeviceDimensions(
    /** Device type (e.g., "mobile", "tablet", "desktop") */
    val deviceType: String,
    
    /** Operating system (e.g., "Android", "iOS", "Windows") */
    val os: String,
    
    /** Browser or app name (e.g., "Chrome", "Safari", "NewsPublisher App") */
    val browser: String,
    
    /** Whether the user is accessing via a native app */
    val isNativeApp: Boolean
)
