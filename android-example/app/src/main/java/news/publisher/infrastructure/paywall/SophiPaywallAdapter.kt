package news.publisher.infrastructure.paywall

import android.content.Context
import android.net.Uri
import io.sophi.paywall.PaywallDecider
import io.sophi.paywall.PaywallDeciderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import news.publisher.domain.paywall.PaywallDecisionService
import news.publisher.domain.paywall.WallDecision
import news.publisher.domain.paywall.WallType

/**
 * Adapter that integrates the Sophi Paywall library with our domain layer.
 * 
 * This adapter implements the domain's PaywallDecisionService interface using
 * the Sophi library, following the Adapter pattern to keep the domain layer
 * independent of external libraries.
 * 
 * INTEGRATION STEPS:
 * 1. Initialize PaywallDeciderRepository with host configuration
 * 2. Create PaywallDecider instance for making decisions
 * 3. Provide user and device dimensions via repositories
 * 4. Call decide() and map results to domain model
 */
class SophiPaywallAdapter(
    private val context: Context,
    private val hostId: String
) : PaywallDecisionService {
    
    private val userDimensionRepo = UserDimensionRepositoryImpl(context)
    private val deviceDimensionRepo = DeviceDimensionRepositoryImpl(context)
    
    private val paywallDecider: PaywallDecider by lazy {
        // Initialize the Sophi Paywall library
        // In production, you would configure this with your actual host ID
        val repository = PaywallDeciderRepository()
        repository.create(hostId)
    }
    
    /**
     * Makes a paywall decision for the given content.
     * 
     * This method:
     * 1. Gathers user and device dimensions
     * 2. Calls the Sophi library's decide() function
     * 3. Maps the Sophi response to our domain model
     * 
     * @param contentId Unique identifier for the content
     * @param assignedGroup Optional A/B test group (e.g., "variant", "control")
     * @return WallDecision containing the decision and tracking data
     */
    override suspend fun decide(contentId: String, assignedGroup: String?): WallDecision = withContext(Dispatchers.IO) {
        // Get user dimensions (with referrer detection)
        val userDimensions = userDimensionRepo.getAll()
        
        // Get device dimensions
        val deviceDimensions = deviceDimensionRepo.getAll()
        
        // Call Sophi library
        // NOTE: This is pseudocode - actual Sophi library API may differ
        // Refer to Sophi documentation for exact method signatures
        val sophiDecision = paywallDecider.decide(
            contentId = contentId,
            userDimensions = mapUserDimensions(userDimensions),
            deviceDimensions = io.sophi.paywall.DeviceDimensions(
                hourOfDay = deviceDimensions.hourOfDay,
                os = deviceDimensions.os,
                viewer = deviceDimensions.viewer
            ),
            assignedGroup = assignedGroup
        )
        
        // Map Sophi response to domain model
        WallDecision(
            contentId = contentId,
            shouldShowWall = sophiDecision.outcome.wallVisibility == "always",
            wallType = when (sophiDecision.outcome.wallType) {
                "paywall" -> WallType.PAYWALL
                "regwall" -> WallType.REGWALL
                else -> WallType.NONE
            },
            trace = sophiDecision.trace,
            context = sophiDecision.context,
            inputs = sophiDecision.inputs,
            experimentGroup = sophiDecision.experiment?.assignedGroup
        )
    }
    
    /**
     * Maps our domain UserDimensions to Sophi library format.
     * 
     * This mapping isolates the domain from Sophi library types.
     */
    private fun mapUserDimensions(dimensions: UserDimensions): io.sophi.paywall.UserDimensions {
        return io.sophi.paywall.UserDimensions(
            todayPageViews = dimensions.todayPageViews,
            todayPageViewsByArticle = dimensions.todayPageViewsByArticle,
            todayPageViewsByArticleWithPaywall = dimensions.todayPageViewsByArticleWithPaywall,
            todayPageViewsByArticleWithRegwall = dimensions.todayPageViewsByArticleWithRegwall,
            todayTopLevelSections = dimensions.todayTopLevelSections,
            todayTopLevelSectionsByArticle = dimensions.todayTopLevelSectionsByArticle,
            sevenDayPageViews = dimensions.sevenDayPageViews,
            sevenDayPageViewsByArticle = dimensions.sevenDayPageViewsByArticle,
            sevenDayPageViewsByArticleWithPaywall = dimensions.sevenDayPageViewsByArticleWithPaywall,
            sevenDayPageViewsByArticleWithRegwall = dimensions.sevenDayPageViewsByArticleWithRegwall,
            sevenDayTopLevelSections = dimensions.sevenDayTopLevelSections,
            sevenDayTopLevelSectionsByArticle = dimensions.sevenDayTopLevelSectionsByArticle,
            sevenDayVisitCount = dimensions.sevenDayVisitCount,
            twentyEightDayPageViews = dimensions.twentyEightDayPageViews,
            twentyEightDayPageViewsByArticle = dimensions.twentyEightDayPageViewsByArticle,
            twentyEightDayPageViewsByArticleWithPaywall = dimensions.twentyEightDayPageViewsByArticleWithPaywall,
            twentyEightDayPageViewsByArticleWithRegwall = dimensions.twentyEightDayPageViewsByArticleWithRegwall,
            twentyEightDayTopLevelSections = dimensions.twentyEightDayTopLevelSections,
            twentyEightDayTopLevelSectionsByArticle = dimensions.twentyEightDayTopLevelSectionsByArticle,
            twentyEightDayVisitCount = dimensions.twentyEightDayVisitCount,
            daysSinceLastVisit = dimensions.daysSinceLastVisit,
            visitorType = dimensions.visitorType.value,
            timezone = dimensions.timezone,
            pageReferrer = dimensions.pageReferrer,
            sessionReferrer = dimensions.sessionReferrer
        )
    }
    
}
