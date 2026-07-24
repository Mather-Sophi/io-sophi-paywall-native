package news.publisher.infrastructure.paywall

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementation of user dimension repository.
 * 
 * This repository provides user-level metrics to the Sophi Paywall library.
 * In a production app, this would integrate with your analytics system.
 * 
 * For this example, we demonstrate:
 * 1. How to structure the dimension data
 * 2. How to detect referrer information from intent data
 * 3. How to track metrics over time using DataStore
 */
class UserDimensionRepositoryImpl(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_metrics")
    
    /**
     * Gets all user dimensions for the current visitor.
     * 
     * This method is called by the Sophi library when making paywall decisions.
     */
    suspend fun getAll(referrerUri: Uri? = null): UserDimensions {
        val prefs = context.dataStore.data.first()
        
        // Detect referrer information from the URI
        val (medium, source, channel) = detectReferrer(referrerUri)
        
        return UserDimensions(
            // 1-day metrics
            todayPageViews = prefs[intPreferencesKey("today_page_views")] ?: 0,
            todayPageViewsByArticle = prefs[intPreferencesKey("today_article_views")] ?: 0,
            todayPageViewsByArticleWithPaywall = prefs[intPreferencesKey("today_paywall_views")] ?: 0,
            todayPageViewsByArticleWithRegwall = prefs[intPreferencesKey("today_regwall_views")] ?: 0,
            todayTopLevelSections = prefs[intPreferencesKey("today_sections")] ?: 0,
            todayTopLevelSectionsByArticle = prefs[intPreferencesKey("today_article_sections")] ?: 0,
            
            // 7-day metrics
            sevenDayPageViews = prefs[intPreferencesKey("seven_day_page_views")] ?: 0,
            sevenDayPageViewsByArticle = prefs[intPreferencesKey("seven_day_article_views")] ?: 0,
            sevenDayPageViewsByArticleWithPaywall = prefs[intPreferencesKey("seven_day_paywall_views")] ?: 0,
            sevenDayPageViewsByArticleWithRegwall = prefs[intPreferencesKey("seven_day_regwall_views")] ?: 0,
            sevenDayTopLevelSections = prefs[intPreferencesKey("seven_day_sections")] ?: 0,
            sevenDayTopLevelSectionsByArticle = prefs[intPreferencesKey("seven_day_article_sections")] ?: 0,
            sevenDayVisitCount = prefs[intPreferencesKey("seven_day_visit_count")] ?: 0,
            
            // 28-day metrics
            twentyEightDayPageViews = prefs[intPreferencesKey("twenty_eight_day_page_views")] ?: 0,
            twentyEightDayPageViewsByArticle = prefs[intPreferencesKey("twenty_eight_day_article_views")] ?: 0,
            twentyEightDayPageViewsByArticleWithPaywall = prefs[intPreferencesKey("twenty_eight_day_paywall_views")] ?: 0,
            twentyEightDayPageViewsByArticleWithRegwall = prefs[intPreferencesKey("twenty_eight_day_regwall_views")] ?: 0,
            twentyEightDayTopLevelSections = prefs[intPreferencesKey("twenty_eight_day_sections")] ?: 0,
            twentyEightDayTopLevelSectionsByArticle = prefs[intPreferencesKey("twenty_eight_day_article_sections")] ?: 0,
            twentyEightDayVisitCount = prefs[intPreferencesKey("twenty_eight_day_visit_count")] ?: 0,
            
            // Current session context
            visitorType = if (prefs[stringPreferencesKey("is_registered")] == "true") {
                VisitorType.REGISTERED
            } else {
                VisitorType.ANONYMOUS
            },
            referrerMedium = medium,
            referrerSource = source,
            referrerChannel = channel
        )
    }
    
    /**
     * Detects referrer information from a URI.
     * 
     * PRODUCTION IMPLEMENTATION NOTES:
     * - In a real app, you would parse the referrer from Intent data or deep link parameters
     * - You might use UTM parameters (utm_source, utm_medium, utm_campaign)
     * - You would track the HTTP Referer header for web views
     * 
     * EXAMPLES OF REFERRER DETECTION:
     * 
     * 1. SOCIAL MEDIA:
     *    - facebook.com → (SOCIAL, FACEBOOK, null)
     *    - instagram.com → (SOCIAL, INSTAGRAM, null)
     *    - reddit.com → (SOCIAL, REDDIT, null)
     *    - linkedin.com → (SOCIAL, LINKEDIN, null)
     *    - twitter.com or t.co → (SOCIAL, X, null)
     * 
     * 2. SEARCH ENGINES:
     *    - google.com/search → (SEARCH, GOOGLE, SEARCH)
     *    - news.google.com → (SEARCH, GOOGLE, NEWS)
     *    - googleusercontent.com (Discover) → (SEARCH, GOOGLE, DISCOVER)
     *    - bing.com → (SEARCH, BING, null)
     *    - yahoo.com → (SEARCH, YAHOO, null)
     *    - duckduckgo.com → (SEARCH, DUCKDUCKGO, null)
     * 
     * 3. CAMPAIGNS:
     *    - UTM parameters present → (CAMPAIGN, NEWSLETTER, null)
     *    - Email link with campaign ID → (CAMPAIGN, NEWSLETTER, null)
     * 
     * 4. DIRECT:
     *    - No referrer → (DIRECT, null, null)
     *    - Bookmark → (DIRECT, null, null)
     * 
     * 5. INTERNAL:
     *    - Same domain → (INTERNAL, null, null)
     */
    private fun detectReferrer(uri: Uri?): Triple<ReferrerMedium, ReferrerSource?, ReferrerChannel?> {
        if (uri == null) {
            return Triple(ReferrerMedium.DIRECT, null, null)
        }
        
        val host = uri.host?.lowercase() ?: return Triple(ReferrerMedium.DIRECT, null, null)
        val path = uri.path?.lowercase() ?: ""
        
        // Check for UTM parameters (campaign tracking)
        val utmSource = uri.getQueryParameter("utm_source")
        val utmMedium = uri.getQueryParameter("utm_medium")
        if (utmSource != null || utmMedium != null) {
            return Triple(ReferrerMedium.CAMPAIGN, ReferrerSource.NEWSLETTER, null)
        }
        
        // Detect social media sources
        when {
            host.contains("facebook.com") || host.contains("fb.com") -> 
                return Triple(ReferrerMedium.SOCIAL, ReferrerSource.FACEBOOK, null)
            host.contains("instagram.com") -> 
                return Triple(ReferrerMedium.SOCIAL, ReferrerSource.INSTAGRAM, null)
            host.contains("twitter.com") || host.contains("t.co") -> 
                return Triple(ReferrerMedium.SOCIAL, ReferrerSource.X, null)
            host.contains("linkedin.com") -> 
                return Triple(ReferrerMedium.SOCIAL, ReferrerSource.LINKEDIN, null)
            host.contains("reddit.com") -> 
                return Triple(ReferrerMedium.SOCIAL, ReferrerSource.REDDIT, null)
        }
        
        // Detect search engines and channels
        when {
            host.contains("google.com") -> {
                val channel = when {
                    host.contains("news.google.com") -> ReferrerChannel.NEWS
                    path.contains("/search") -> ReferrerChannel.SEARCH
                    else -> null
                }
                return Triple(ReferrerMedium.SEARCH, ReferrerSource.GOOGLE, channel)
            }
            host.contains("googleusercontent.com") -> 
                return Triple(ReferrerMedium.SEARCH, ReferrerSource.GOOGLE, ReferrerChannel.DISCOVER)
            host.contains("bing.com") -> 
                return Triple(ReferrerMedium.SEARCH, ReferrerSource.BING, null)
            host.contains("yahoo.com") -> 
                return Triple(ReferrerMedium.SEARCH, ReferrerSource.YAHOO, null)
            host.contains("duckduckgo.com") -> 
                return Triple(ReferrerMedium.SEARCH, ReferrerSource.DUCKDUCKGO, null)
        }
        
        // Check if internal (same domain)
        if (host.contains("publisher.news") || host.contains("news.publisher")) {
            return Triple(ReferrerMedium.INTERNAL, null, null)
        }
        
        // Default to OTHER for unknown sources
        return Triple(ReferrerMedium.OTHER, null, null)
    }
    
    /**
     * Increments page view metrics.
     * 
     * Call this method whenever a page is viewed to update the metrics.
     */
    suspend fun trackPageView(isArticle: Boolean, section: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[intPreferencesKey("today_page_views")] = 
                (prefs[intPreferencesKey("today_page_views")] ?: 0) + 1
            
            if (isArticle) {
                prefs[intPreferencesKey("today_article_views")] = 
                    (prefs[intPreferencesKey("today_article_views")] ?: 0) + 1
            }
        }
    }
}
