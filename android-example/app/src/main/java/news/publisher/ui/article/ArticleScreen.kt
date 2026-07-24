package news.publisher.ui.article

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import news.publisher.domain.paywall.WallType
import news.publisher.infrastructure.paywall.SophiPaywallAdapter

/**
 * Article screen demonstrating Sophi Paywall integration.
 * 
 * This screen shows:
 * 1. How to get a paywall decision
 * 2. How to display the decision result
 * 3. How to track the decision in analytics
 */
@Composable
fun ArticleScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // In a production app, you would inject this via DI (Hilt/Koin)
    val paywallService = SophiPaywallAdapter(
        context = context,
        hostId = "your-host-id" // Replace with actual host ID
    )
    
    val viewModel: ArticleViewModel = viewModel(
        factory = ArticleViewModelFactory(paywallService)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is ArticleUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
        is ArticleUiState.Success -> {
            ArticleContent(
                article = state.article,
                wallDecision = state.wallDecision,
                modifier = modifier
            )
        }
        is ArticleUiState.Error -> {
            Text("Error: ${state.message}")
        }
    }
}

@Composable
private fun ArticleContent(
    article: news.publisher.domain.model.Article,
    wallDecision: news.publisher.domain.paywall.WallDecision,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Article header
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = article.section,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Divider()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Paywall decision info (for demonstration)
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
                
                if (wallDecision.experimentGroup != null) {
                    Text("Experiment Group: ${wallDecision.experimentGroup}")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tracking Data",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Trace: ${wallDecision.trace.take(50)}...")
                Text("Context: ${wallDecision.context.take(50)}...")
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
    private val paywallService: news.publisher.domain.paywall.PaywallDecisionService
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ArticleViewModel(paywallService) as T
    }
}
