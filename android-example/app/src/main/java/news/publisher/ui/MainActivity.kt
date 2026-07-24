package news.publisher.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import news.publisher.ui.article.ArticleScreen
import news.publisher.ui.theme.NewsPublisherTheme

/**
 * Main activity for the News Publisher app.
 * 
 * This activity demonstrates the Sophi Paywall integration in a simple article view.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            NewsPublisherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ArticleScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
