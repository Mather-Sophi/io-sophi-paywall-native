package news.publisher.ui.article

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import news.publisher.domain.model.Article
import news.publisher.domain.paywall.WallDecision
import news.publisher.domain.paywall.WallType
import news.publisher.infrastructure.paywall.SophiPaywallAdapter

/**
 * Article screen demonstrating Sophi Paywall integration.
 *
 * This screen shows:
 * 1. How to get a paywall decision, passing contentProperties for the article
 * 2. How to display the full decision result for debugging
 * 3. How to show paywall/regwall UI based on the decision
 */
@Composable
fun ArticleScreen(
    article: Article,
    paywallService: SophiPaywallAdapter,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ArticleViewModel = viewModel(
        factory = ArticleViewModelFactory(article, paywallService)
    )

    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(article.section) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when (val state = uiState) {
            is ArticleUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ArticleUiState.Success -> {
                ArticleContent(article = state.article, wallDecision = state.wallDecision)
            }
            is ArticleUiState.Error -> {
                Text("Error: ${state.message}", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ArticleContent(
    article: Article,
    wallDecision: WallDecision,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Paywall decision debug panel — useful during integration; hide or
        // gate behind a debug flag in production.
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (wallDecision.wallType) {
                    WallType.PAYWALL -> MaterialTheme.colorScheme.errorContainer
                    WallType.REGWALL -> MaterialTheme.colorScheme.tertiaryContainer
                    WallType.NONE -> MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Paywall Decision",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Show Wall: ${wallDecision.shouldShowWall}")
                Text("Wall Type: ${wallDecision.wallType}")
                Text("Content ID: ${wallDecision.contentId}")
                Text("Paywall Score: ${wallDecision.paywallScore ?: "N/A"}")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tracking Data",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Trace: ${wallDecision.trace}")
                Text("Context: ${wallDecision.context}")
                Text("Inputs: ${wallDecision.inputs}")
                Text("User Properties: ${wallDecision.userProperties ?: "N/A"}")
                Text("Content Properties: ${wallDecision.contentProperties ?: "N/A"}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Article content
        Text(
            text = article.content,
            style = MaterialTheme.typography.bodyLarge
        )

        // Show paywall UI if decision says to show wall
        if (wallDecision.shouldShowWall) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (wallDecision.wallType) {
                            WallType.PAYWALL -> "Subscribe to continue reading"
                            WallType.REGWALL -> "Register to continue reading"
                            WallType.NONE -> ""
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This is a premium article. ${
                            when (wallDecision.wallType) {
                                WallType.PAYWALL -> "Subscribe now to access unlimited content."
                                WallType.REGWALL -> "Create a free account to continue."
                                WallType.NONE -> ""
                            }
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Handle subscription/registration */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when (wallDecision.wallType) {
                                WallType.PAYWALL -> "Subscribe Now"
                                WallType.REGWALL -> "Register Free"
                                WallType.NONE -> ""
                            }
                        )
                    }
                }
            }
        }
    }
}

// Simple factory for demonstration
class ArticleViewModelFactory(
    private val article: Article,
    private val paywallService: SophiPaywallAdapter,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ArticleViewModel(article, paywallService) as T
    }
}
