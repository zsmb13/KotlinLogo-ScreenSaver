package util

import config.Preferences
import platform.Foundation.NSLog

inline fun debugLog(lazyMessage: () -> String) {
    if (Preferences.IS_DEBUG) {
        NSLog("KOTLIN: ${lazyMessage()}")
    }
}
