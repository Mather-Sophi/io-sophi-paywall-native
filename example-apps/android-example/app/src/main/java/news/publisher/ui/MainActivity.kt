package news.publisher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import news.publisher.domain.model.Article
import news.publisher.infrastructure.paywall.SophiPaywallAdapter
import news.publisher.ui.article.ArticleScreen
import news.publisher.ui.home.HomeScreen
import news.publisher.ui.theme.NewsPublisherTheme

/**
 * Main activity for the News Publisher app.
 *
 * Demonstrates a small two-screen flow: a Home screen (sample article list +
 * Sign In/Sign Out) and an Article screen (paywall decision + debug panel).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NewsPublisherTheme {
                val context = LocalContext.current

                // In a production app, inject this via DI (Hilt/Koin) instead
                // of creating it here; it's created once for the Activity's
                // lifetime rather than once per screen.
                val paywallService = remember {
                    SophiPaywallAdapter(
                        context = context,
                        hostId = "your-host-id" // Replace with actual host ID
                    )
                }

                var selectedArticle by remember { mutableStateOf<Article?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val article = selectedArticle
                    if (article == null) {
                        HomeScreen(
                            paywallService = paywallService,
                            onArticleSelected = { selectedArticle = it },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        ArticleScreen(
                            article = article,
                            paywallService = paywallService,
                            onBack = { selectedArticle = null },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
