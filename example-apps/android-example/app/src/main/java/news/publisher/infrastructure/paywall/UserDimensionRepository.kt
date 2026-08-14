package news.publisher.infrastructure.paywall

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Implementation of user dimension repository.
 *
 * This repository provides user-level metrics to the Sophi Paywall library.
 *
 * Unlike a flat/mock implementation, this demonstrates a REAL rolling-window
 * engagement tracker: one [DayBucket] is recorded per calendar day (today +
 * up to 28 days of history), rolled forward automatically as days pass, and
 * summed into disjoint today/7-day/28-day windows the SDK expects. This
 * mirrors the day-bucketing algorithm Sophi's own web SDK uses.
 *
 * There is no JSON/serialization library in this project's dependencies, so
 * buckets are hand-serialized into a compact delimited string. In your app,
 * feel free to swap this for Room, SQLDelight, or a JSON library — the part
 * worth keeping is the day-bucketing and disjoint-window-sum algorithm.
 */
class UserDimensionRepositoryImpl(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_metrics")

    /**
     * Gets all user dimensions for the current visitor.
     *
     * Rolls the day-bucket history forward if the calendar day has changed
     * since it was last written, then sums the buckets into the shape the
     * Sophi library expects. This method is read-only — it does not persist
     * the rollover; the next [trackPageView]/[trackWallView] call does that.
     */
    suspend fun getAll(referrerUri: Uri? = null): UserDimensions {
        val prefs = context.dataStore.data.first()
        val referrer = referrerUri?.toString()

        val buckets = rollForNewDay(prefs)
        val summary = summarize(buckets)

        return UserDimensions(
            todayPageViews = summary.today.pageViews,
            todayPageViewsByArticle = summary.today.pageViewsByArticle,
            todayPageViewsByArticleWithPaywall = summary.today.pageViewsByArticleWithPaywall,
            todayPageViewsByArticleWithRegwall = summary.today.pageViewsByArticleWithRegwall,
            todayTopLevelSections = summary.today.topLevelSections.size,
            todayTopLevelSectionsByArticle = summary.today.topLevelSectionsByArticle.size,

            sevenDayPageViews = summary.sevenDay.pageViews,
            sevenDayPageViewsByArticle = summary.sevenDay.pageViewsByArticle,
            sevenDayPageViewsByArticleWithPaywall = summary.sevenDay.pageViewsByArticleWithPaywall,
            sevenDayPageViewsByArticleWithRegwall = summary.sevenDay.pageViewsByArticleWithRegwall,
            sevenDayTopLevelSections = summary.sevenDay.topLevelSections.size,
            sevenDayTopLevelSectionsByArticle = summary.sevenDay.topLevelSectionsByArticle.size,
            sevenDayVisitCount = summary.sevenDay.visitCount,

            twentyEightDayPageViews = summary.twentyEightDay.pageViews,
            twentyEightDayPageViewsByArticle = summary.twentyEightDay.pageViewsByArticle,
            twentyEightDayPageViewsByArticleWithPaywall = summary.twentyEightDay.pageViewsByArticleWithPaywall,
            twentyEightDayPageViewsByArticleWithRegwall = summary.twentyEightDay.pageViewsByArticleWithRegwall,
            twentyEightDayTopLevelSections = summary.twentyEightDay.topLevelSections.size,
            twentyEightDayTopLevelSectionsByArticle = summary.twentyEightDay.topLevelSectionsByArticle.size,
            twentyEightDayVisitCount = summary.twentyEightDay.visitCount,

            daysSinceLastVisit = summary.daysSinceLastVisit,
            visitorType = if (prefs[VISITOR_TYPE_KEY] == VisitorType.REGISTERED.value) {
                VisitorType.REGISTERED
            } else {
                VisitorType.ANONYMOUS
            },
            timezone = prefs[TIMEZONE_KEY] ?: TimeZone.getDefault().id,
            pageReferrer = referrer,
            sessionReferrer = referrer
        )
    }

    /** Flips the visitor between anonymous and registered — wire to a Sign In/Sign Out control. */
    suspend fun setVisitorType(visitorType: VisitorType) {
        context.dataStore.edit { prefs ->
            prefs[VISITOR_TYPE_KEY] = visitorType.value
        }
    }

    /**
     * Records a page view in today's bucket.
     *
     * Call this whenever the user views a page in your app.
     */
    suspend fun trackPageView(isArticle: Boolean, section: String? = null) {
        updateToday { today ->
            today.copy(
                pageViews = today.pageViews + 1,
                pageViewsByArticle = today.pageViewsByArticle + if (isArticle) 1 else 0,
                topLevelSections = section?.let { today.topLevelSections + it } ?: today.topLevelSections,
                topLevelSectionsByArticle = if (isArticle && section != null) {
                    today.topLevelSectionsByArticle + section
                } else {
                    today.topLevelSectionsByArticle
                }
            )
        }
    }

    /** Records a paywall/regwall impression in today's bucket. */
    suspend fun trackWallView(wallType: WallType, section: String? = null) {
        if (wallType == WallType.NONE) return
        updateToday { today ->
            today.copy(
                pageViewsByArticleWithPaywall = today.pageViewsByArticleWithPaywall +
                    if (wallType == WallType.PAYWALL) 1 else 0,
                pageViewsByArticleWithRegwall = today.pageViewsByArticleWithRegwall +
                    if (wallType == WallType.REGWALL) 1 else 0,
                topLevelSectionsByArticle = section?.let { today.topLevelSectionsByArticle + it }
                    ?: today.topLevelSectionsByArticle
            )
        }
    }

    private suspend fun updateToday(mutate: (DayBucket) -> DayBucket) {
        context.dataStore.edit { prefs ->
            val buckets = rollForNewDay(prefs)
            val today = mutate(buckets.first())
            prefs[DAY_BUCKETS_KEY] = encodeBuckets(listOf(today) + buckets.drop(1))
            prefs[LAST_SEEN_DATE_KEY] = todayDateString()
        }
    }

    /** Rolls the stored bucket history forward by however many calendar days have elapsed. */
    private fun rollForNewDay(prefs: Preferences): List<DayBucket> {
        val stored = decodeBuckets(prefs[DAY_BUCKETS_KEY])
        val daysElapsed = daysBetween(prefs[LAST_SEEN_DATE_KEY] ?: "")
        if (daysElapsed <= 0) return stored

        val rolled = buildList {
            add(DayBucket())
            repeat(daysElapsed - 1) { add(DayBucket()) }
            addAll(stored)
        }
        return rolled.take(MAX_STORED_DAYS)
    }

    private data class WindowSum(
        val pageViews: Int = 0,
        val pageViewsByArticle: Int = 0,
        val pageViewsByArticleWithPaywall: Int = 0,
        val pageViewsByArticleWithRegwall: Int = 0,
        val topLevelSections: Set<String> = emptySet(),
        val topLevelSectionsByArticle: Set<String> = emptySet(),
        val visitCount: Int = 0,
    )

    private data class Summary(
        val today: WindowSum,
        val sevenDay: WindowSum,
        val twentyEightDay: WindowSum,
        val daysSinceLastVisit: Int,
    )

    /**
     * Sums the day-bucket array into disjoint today/7-day/28-day windows.
     *
     * Same algorithm as the web SDK's `sumDayMetrics`: bucket index 0 is
     * "today", indices 1-7 are the past 7 days, and indices 8-28 are the
     * past 28 days (each window excludes the ones before it).
     */
    private fun summarize(buckets: List<DayBucket>): Summary {
        val windowBoundaries = intArrayOf(1, 8, 29)
        val windows = mutableListOf<WindowSum>()
        var running = WindowSum()
        var sections = mutableSetOf<String>()
        var articleSections = mutableSetOf<String>()
        var daysSinceLastVisit = 0

        for (i in 0 until minOf(buckets.size, 29)) {
            val bucket = buckets[i]
            sections.addAll(bucket.topLevelSections)
            articleSections.addAll(bucket.topLevelSectionsByArticle)
            running = running.copy(
                pageViews = running.pageViews + bucket.pageViews,
                pageViewsByArticle = running.pageViewsByArticle + bucket.pageViewsByArticle,
                pageViewsByArticleWithPaywall = running.pageViewsByArticleWithPaywall + bucket.pageViewsByArticleWithPaywall,
                pageViewsByArticleWithRegwall = running.pageViewsByArticleWithRegwall + bucket.pageViewsByArticleWithRegwall,
                topLevelSections = sections.toSet(),
                topLevelSectionsByArticle = articleSections.toSet(),
                visitCount = running.visitCount + if (bucket.pageViews > 0) 1 else 0,
            )

            if (bucket.pageViews > 0 && i > 0 && daysSinceLastVisit == 0) {
                daysSinceLastVisit = i
            }

            if (windowBoundaries.contains(i + 1)) {
                windows.add(running)
                running = WindowSum()
                sections = mutableSetOf()
                articleSections = mutableSetOf()
            }
        }
        while (windows.size < windowBoundaries.size) {
            windows.add(running)
        }

        return Summary(
            today = windows[0],
            sevenDay = windows[1],
            twentyEightDay = windows[2],
            daysSinceLastVisit = daysSinceLastVisit,
        )
    }

    private fun dateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun todayDateString(): String = dateFormat().format(Calendar.getInstance().time)

    private fun daysBetween(lastSeenDate: String): Int {
        if (lastSeenDate.isBlank()) return 0
        return try {
            val fmt = dateFormat()
            val then = fmt.parse(lastSeenDate) ?: return 0
            val now = fmt.parse(todayDateString()) ?: return 0
            ((now.time - then.time) / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    private fun encodeBuckets(buckets: List<DayBucket>): String =
        buckets.joinToString(BUCKET_SEP) { it.encode() }

    private fun decodeBuckets(raw: String?): List<DayBucket> {
        if (raw.isNullOrEmpty()) return listOf(DayBucket())
        val decoded = raw.split(BUCKET_SEP).filter { it.isNotEmpty() }.map { DayBucket.decode(it) }
        return decoded.ifEmpty { listOf(DayBucket()) }
    }

    companion object {
        private val DAY_BUCKETS_KEY = stringPreferencesKey("day_buckets")
        private val LAST_SEEN_DATE_KEY = stringPreferencesKey("last_seen_date")
        private val VISITOR_TYPE_KEY = stringPreferencesKey("visitor_type")
        private val TIMEZONE_KEY = stringPreferencesKey("timezone")

        /** today + up to 28 days of history */
        private const val MAX_STORED_DAYS = 29
        private const val BUCKET_SEP = "\n"
    }
}

/**
 * One calendar day's worth of raw engagement counters.
 *
 * [UserDimensionRepositoryImpl] keeps a rolling array of these (today + up
 * to 28 days of history, newest first) and sums disjoint windows of it to
 * produce the today/7-day/28-day dimensions the SDK expects.
 */
private data class DayBucket(
    val pageViews: Int = 0,
    val pageViewsByArticle: Int = 0,
    val pageViewsByArticleWithPaywall: Int = 0,
    val pageViewsByArticleWithRegwall: Int = 0,
    val topLevelSections: Set<String> = emptySet(),
    val topLevelSectionsByArticle: Set<String> = emptySet(),
) {
    fun encode(): String {
        val sections = topLevelSections.joinToString(SECTION_SEP)
        val articleSections = topLevelSectionsByArticle.joinToString(SECTION_SEP)
        return listOf(
            pageViews,
            pageViewsByArticle,
            pageViewsByArticleWithPaywall,
            pageViewsByArticleWithRegwall,
            sections,
            articleSections
        ).joinToString(FIELD_SEP)
    }

    companion object {
        private const val FIELD_SEP = "|"
        private const val SECTION_SEP = ","

        fun decode(raw: String): DayBucket {
            val parts = raw.split(FIELD_SEP, limit = 6)

            fun sectionsAt(index: Int): Set<String> =
                parts.getOrNull(index)?.split(SECTION_SEP)?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

            return DayBucket(
                pageViews = parts.getOrNull(0)?.toIntOrNull() ?: 0,
                pageViewsByArticle = parts.getOrNull(1)?.toIntOrNull() ?: 0,
                pageViewsByArticleWithPaywall = parts.getOrNull(2)?.toIntOrNull() ?: 0,
                pageViewsByArticleWithRegwall = parts.getOrNull(3)?.toIntOrNull() ?: 0,
                topLevelSections = sectionsAt(4),
                topLevelSectionsByArticle = sectionsAt(5),
            )
        }
    }
}
