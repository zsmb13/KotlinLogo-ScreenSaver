package util

import config.DEBUG_MODE

actual inline fun debugLog(lazyMessage: () -> String) {
    if (DEBUG_MODE) {
        println(lazyMessage())
    }
}
