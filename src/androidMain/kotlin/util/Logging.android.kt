package util

import android.util.Log

actual inline fun debugLog(lazyMessage: () -> String) {
    Log.d("KotlinLogoWallpaper", lazyMessage())
}
