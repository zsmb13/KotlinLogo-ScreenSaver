package util

import config.Preferences
import platform.Foundation.NSLog

var ispreview = false

inline fun debugLog(lazyMessage: () -> String) {
    if (Preferences.IS_DEBUG && !ispreview) {
        NSLog("KOTLIN: ${lazyMessage()}")
    }
}
