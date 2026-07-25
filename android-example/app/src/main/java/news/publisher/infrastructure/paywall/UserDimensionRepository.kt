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
        val referrer = referrerUri?.toString()
        
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

            daysSinceLastVisit = prefs[intPreferencesKey("days_since_last_visit")] ?: 0,
            visitorType = if (prefs[stringPreferencesKey("is_registered")] == "true") {
                VisitorType.REGISTERED
            } else {
                VisitorType.ANONYMOUS
            },
            timezone = prefs[stringPreferencesKey("timezone")] ?: "UTC",
            pageReferrer = referrer,
            sessionReferrer = referrer
        )
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
