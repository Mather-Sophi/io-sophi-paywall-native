package news.publisher.ui.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import news.publisher.domain.model.Article
import news.publisher.domain.paywall.WallDecision
import news.publisher.domain.paywall.WallType
import news.publisher.infrastructure.paywall.SophiPaywallAdapter

/**
 * ViewModel for the Article screen.
 *
 * Demonstrates how to integrate paywall decisions into your app's business
 * logic: passing contentProperties for the article being viewed, then
 * tracking the resulting page/wall view back into the rolling-window
 * repository so future decisions reflect real engagement.
 */
class ArticleViewModel(
    private val article: Article,
    private val paywallService: SophiPaywallAdapter,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleUiState>(ArticleUiState.Loading)
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            try {
                val decision = paywallService.decide(
                    contentId = article.id,
                    contentProperties = mapOf(
                        "section" to article.section,
                        "isPremium" to article.isPremium,
                    ),
                )

                // Track the view now that we know the outcome, so the next
                // decision (for this or any other article) reflects it.
                paywallService.trackPageView(isArticle = true, section = article.section)
                if (decision.wallType != WallType.NONE) {
                    paywallService.trackWallView(decision.wallType, section = article.section)
                }

                _uiState.value = ArticleUiState.Success(article, decision)
            } catch (error: Exception) {
                _uiState.value = ArticleUiState.Error(error.message ?: "Failed to load paywall decision")
            }
        }
    }
}

sealed class ArticleUiState {
    object Loading : ArticleUiState()
    data class Success(val article: Article, val wallDecision: WallDecision) : ArticleUiState()
    data class Error(val message: String) : ArticleUiState()
}
