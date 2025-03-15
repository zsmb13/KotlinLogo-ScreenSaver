package util

import config.DEBUG_MODE
import platform.Foundation.NSLog

actual inline fun debugLog(lazyMessage: () -> String) {
    if (DEBUG_MODE) {
        NSLog("KOTLIN: ${lazyMessage()}")
    }
}
