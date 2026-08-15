package news.publisher.infrastructure.paywall

import android.content.Context
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

    private val paywallDeciderRepository: PaywallDeciderRepository =
        PaywallDeciderRepository.createNew(userDimensionRepo, deviceDimensionRepo)

    /**
     * Makes a paywall decision for the given content.
     *
     * This method:
     * 1. Resolves a host-scoped decider
     * 2. Calls the Sophi library's decide() function
     * 3. Maps the Sophi response to our domain model
     *
     * @param contentId Unique identifier for the content
     * @param contentProperties Optional content-level properties (e.g. section, tags)
     * @param userProperties Optional user-level properties (e.g. subscription tier)
     * @return WallDecision containing the decision and tracking data
     */
    override suspend fun decide(
        contentId: String,
        contentProperties: Map<String, Any>?,
        userProperties: Map<String, Any>?,
    ): WallDecision = withContext(Dispatchers.IO) {
        val paywallDecider = paywallDeciderRepository.getOneByHost(host = hostId)

        val sophiDecision = paywallDecider.decide(
            contentId = contentId,
            contentProperties = contentProperties,
            userProperties = userProperties,
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
            inputs = sophiDecision.inputs ?: "",
            // .toString()/.toDouble() defensively tolerate whatever concrete
            // numeric/property type the SDK returns here.
            paywallScore = sophiDecision.paywallScore?.toDouble(),
            userProperties = sophiDecision.userProperties?.toString(),
            contentProperties = sophiDecision.contentProperties?.toString(),
        )
    }

    /** Flips the current visitor between anonymous/registered — wire to a Sign In/Sign Out control. */
    suspend fun setVisitorType(visitorType: VisitorType) = userDimensionRepo.setVisitorType(visitorType)

    /** Reads the visitor's current sign-in state. */
    suspend fun currentVisitorType(): VisitorType = userDimensionRepo.getAll().visitorType

    /** Records a page view in the rolling-window repository. Call after rendering content. */
    suspend fun trackPageView(isArticle: Boolean, section: String? = null) =
        userDimensionRepo.trackPageView(isArticle, section)

    /** Records a paywall/regwall impression in the rolling-window repository. */
    suspend fun trackWallView(wallType: WallType, section: String? = null) =
        userDimensionRepo.trackWallView(wallType, section)
}
