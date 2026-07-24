package news.publisher.infrastructure.paywall

import android.content.Context
import android.os.Build

/**
 * Implementation of device dimension repository.
 * 
 * This repository provides device-level information to the Sophi Paywall library.
 * These dimensions help the paywall engine understand the user's device context.
 */
class DeviceDimensionRepositoryImpl(private val context: Context) {
    
    /**
     * Gets all device dimensions for the current device.
     * 
     * This method is called by the Sophi library when making paywall decisions.
     */
    fun getAll(): DeviceDimensions {
        return DeviceDimensions(
            deviceType = getDeviceType(),
            os = "Android ${Build.VERSION.RELEASE}",
            browser = "NewsPublisher App",
            isNativeApp = true
        )
    }
    
    /**
     * Determines the device type based on screen size and form factor.
     * 
     * In a production app, you might use more sophisticated detection logic
     * based on screen dimensions, density, and configuration.
     */
    private fun getDeviceType(): String {
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
        
        return when (screenLayout) {
            android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE,
            android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE -> "tablet"
            else -> "mobile"
        }
    }
}
