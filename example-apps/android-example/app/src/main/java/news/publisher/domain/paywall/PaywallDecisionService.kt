package news.publisher.domain.paywall

/**
 * Domain service for making paywall decisions.
 * 
 * This interface defines the contract for paywall decision-making in the domain layer,
 * keeping the domain logic independent of the Sophi library implementation.
 * 
 * The actual implementation (SophiPaywallAdapter) lives in the infrastructure layer.
 */
interface PaywallDecisionService {
    /**
     * Determines whether to show a paywall for the given content.
     *
     * @param contentId Unique identifier for the content
     * @param contentProperties Optional content-level properties (e.g. section, tags)
     * @param userProperties Optional user-level properties (e.g. subscription tier)
     * @return WallDecision containing the decision and tracking information
     */
    suspend fun decide(
        contentId: String,
        contentProperties: Map<String, Any>? = null,
        userProperties: Map<String, Any>? = null,
    ): WallDecision
}
