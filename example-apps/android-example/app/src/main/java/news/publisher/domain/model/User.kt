package news.publisher.domain.model

/**
 * Represents a user/visitor in the publisher's system.
 * 
 * Tracks visitor type and subscription status for paywall decisions.
 */
data class User(
    val visitorId: String,
    val isRegistered: Boolean = false,
    val isSubscriber: Boolean = false
)
