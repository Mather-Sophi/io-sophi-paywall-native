package news.publisher.domain.paywall

/**
 * Represents the result of a paywall decision.
 * 
 * This domain model encapsulates the decision made by the paywall engine,
 * isolating the domain logic from the Sophi library implementation details.
 */
data class WallDecision(
    val contentId: String,
    val shouldShowWall: Boolean,
    val wallType: WallType,
    val trace: String,
    val context: String,
    val inputs: String,
    val experimentGroup: String?
)

enum class WallType {
    NONE,
    PAYWALL,
    REGWALL
}
