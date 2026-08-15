package news.publisher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import news.publisher.domain.model.Article
import news.publisher.infrastructure.paywall.SophiPaywallAdapter
import news.publisher.infrastructure.paywall.VisitorType

/**
 * Home screen: a sample article list plus a Sign In/Sign Out control.
 *
 * Demonstrates how signing in (flipping [VisitorType] to `registered`) can
 * change the paywall decision you get for the exact same content.
 */
@Composable
fun HomeScreen(
    paywallService: SophiPaywallAdapter,
    onArticleSelected: (Article) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(paywallService))
    val visitorType by viewModel.visitorType.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "News Publisher",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (visitorType == VisitorType.REGISTERED) "Signed in" else "Not signed in",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Button(onClick = { viewModel.toggleSignIn() }) {
                Text(if (visitorType == VisitorType.REGISTERED) "Sign Out" else "Sign In")
            }
        }

        Divider()

        LazyColumn {
            items(viewModel.articles) { article ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArticleSelected(article) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = article.section,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (article.isPremium) {
                        Text(
                            text = "PREMIUM",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Divider()
            }
        }
    }
}
