package news.publisher.ui.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import news.publisher.domain.model.Article
import news.publisher.domain.paywall.PaywallDecisionService
import news.publisher.domain.paywall.WallDecision
import news.publisher.domain.paywall.WallType

/**
 * ViewModel for the Article screen.
 * 
 * Demonstrates how to integrate paywall decisions into your app's business logic.
 */
class ArticleViewModel(
    private val paywallService: PaywallDecisionService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ArticleUiState>(ArticleUiState.Loading)
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()
    
    init {
        loadArticle()
    }
    
    private fun loadArticle() {
        viewModelScope.launch {
            // Simulate loading an article
            val article = Article(
                id = "article-123",
                title = "Breaking News: Sophi Paywall Integration Example",
                section = "Technology",
                content = "This is an example article demonstrating the Sophi Paywall integration...",
                isPremium = true
            )
            
            // Get paywall decision
            val decision = paywallService.decide(contentId = article.id)
            
            _uiState.value = ArticleUiState.Success(article, decision)
        }
    }
}

sealed class ArticleUiState {
    object Loading : ArticleUiState()
    data class Success(val article: Article, val wallDecision: WallDecision) : ArticleUiState()
    data class Error(val message: String) : ArticleUiState()
}
