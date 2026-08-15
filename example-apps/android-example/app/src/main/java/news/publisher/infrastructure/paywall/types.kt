package news.publisher.infrastructure.paywall

/**
 * Type definitions for Sophi Paywall user and device dimensions.
 *
 * These mirror the current native SDK repository contracts.
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

    /** Number of days since the visitor's last session */
    val daysSinceLastVisit: Int,

    /** Visitor type: anonymous or registered */
    val visitorType: VisitorType,

    /** IANA timezone, for example "America/New_York" */
    val timezone: String,

    /** Current page referrer URL, if available */
    val pageReferrer: String?,

    /** Session entry referrer URL, if available */
    val sessionReferrer: String?
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
    /** Current hour in 24h format [0-23] */
    val hourOfDay: Int,

    /** Operating system, expected values are lowercase ("android" or "ios") */
    val os: String,

    /** Viewer/app identifier, for example "news-publisher-android-1.0.0" */
    val viewer: String
)
