package util

import config.GlobalPreferences
import platform.Foundation.NSLogv

actual inline fun debugLog(lazyMessage: () -> String) {
//    if (GlobalPreferences.IS_DEBUG) {
//        NSLog("KOTLIN: ${lazyMessage()}")
//    }
}
