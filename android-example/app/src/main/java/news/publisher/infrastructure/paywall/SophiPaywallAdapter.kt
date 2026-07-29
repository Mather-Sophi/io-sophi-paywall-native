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
     * @return WallDecision containing the decision and tracking data
     */
    override suspend fun decide(contentId: String): WallDecision = withContext(Dispatchers.IO) {
        val paywallDecider = paywallDeciderRepository.getOneByHost(host = hostId)

        val sophiDecision = paywallDecider.decide(
            contentId = contentId,
            contentProperties = null,
            userProperties = null
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
            inputs = sophiDecision.inputs ?: ""
        )
    }

}
