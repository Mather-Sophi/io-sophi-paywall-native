package news.publisher.infrastructure.paywall

import android.content.Context
import news.publisher.BuildConfig
import java.util.Calendar

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
            hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            os = "android",
            viewer = "news-publisher-android-${BuildConfig.VERSION_NAME}"
        )
    }
}
