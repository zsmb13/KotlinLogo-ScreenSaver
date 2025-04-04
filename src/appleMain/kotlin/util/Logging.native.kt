package util

import platform.Foundation.NSLog

var _debugLoggingEnabled: Boolean? = null

actual inline fun debugLog(lazyMessage: () -> String) {
    if (_debugLoggingEnabled ?: false) {
        NSLog("KOTLIN: ${lazyMessage()}")
    }
}
