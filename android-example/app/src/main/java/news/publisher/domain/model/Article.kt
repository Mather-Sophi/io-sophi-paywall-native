package news.publisher.domain.model

/**
 * Represents a news article in the publisher's content system.
 * 
 * This is a simplified domain model for demonstration purposes.
 * In a production app, this would include additional fields like author, publish date, etc.
 */
data class Article(
    val id: String,
    val title: String,
    val section: String,
    val content: String,
    val isPremium: Boolean = false
)
