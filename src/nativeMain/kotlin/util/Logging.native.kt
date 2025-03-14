package util

import config.GlobalPreferences
import platform.Foundation.NSLog

actual inline fun debugLog(lazyMessage: () -> String) {
//    if (GlobalPreferences.IS_DEBUG) {
        NSLog("KOTLIN: ${lazyMessage()}")
//    }
}
