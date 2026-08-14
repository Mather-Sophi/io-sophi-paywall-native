package news.publisher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import news.publisher.domain.model.Article
import news.publisher.infrastructure.paywall.SophiPaywallAdapter
import news.publisher.infrastructure.paywall.VisitorType

/**
 * Sample article catalog + visitor sign-in state for the Home screen.
 *
 * Demonstrates how flipping [VisitorType] between anonymous and registered —
 * exactly what a real login/logout flow would do — changes paywall decisions
 * for the same content.
 */
class HomeViewModel(private val paywallService: SophiPaywallAdapter) : ViewModel() {

    val articles: List<Article> = SAMPLE_ARTICLES

    private val _visitorType = MutableStateFlow(VisitorType.ANONYMOUS)
    val visitorType: StateFlow<VisitorType> = _visitorType.asStateFlow()

    init {
        viewModelScope.launch {
            _visitorType.value = paywallService.currentVisitorType()
        }
    }

    fun toggleSignIn() {
        viewModelScope.launch {
            val next = if (_visitorType.value == VisitorType.ANONYMOUS) {
                VisitorType.REGISTERED
            } else {
                VisitorType.ANONYMOUS
            }
            paywallService.setVisitorType(next)
            _visitorType.value = next
        }
    }

    companion object {
        private val SAMPLE_ARTICLES = listOf(
            Article(
                id = "article-city-budget",
                title = "City Council Approves New Budget",
                section = "Politics",
                content = "The city council voted 7-2 to approve next year's budget, prioritizing " +
                    "infrastructure and public safety spending after months of public hearings...",
                isPremium = true
            ),
            Article(
                id = "article-local-team-wins",
                title = "Local Team Clinches Division Title",
                section = "Sports",
                content = "In a thrilling overtime finish, the home team secured the division title " +
                    "for the first time in a decade, sending fans into the streets in celebration...",
                isPremium = false
            ),
            Article(
                id = "article-startup-funding",
                title = "Local Startup Raises Series A",
                section = "Technology",
                content = "A homegrown startup announced a \$12M Series A round led by a national " +
                    "venture firm, with plans to triple headcount over the next year...",
                isPremium = true
            ),
        )
    }
}

class HomeViewModelFactory(
    private val paywallService: SophiPaywallAdapter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(paywallService) as T
    }
}
