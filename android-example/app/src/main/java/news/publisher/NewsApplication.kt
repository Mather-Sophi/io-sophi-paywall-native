package news.publisher

import android.app.Application

/**
 * Application class for the News Publisher app.
 * 
 * This is the entry point for the application. In a production app with dependency injection,
 * you would initialize your DI framework here (e.g., Hilt, Koin).
 */
class NewsApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any app-level dependencies here
        // For example, with Hilt: @HiltAndroidApp annotation
        // Or with Koin: startKoin { ... }
    }
}
