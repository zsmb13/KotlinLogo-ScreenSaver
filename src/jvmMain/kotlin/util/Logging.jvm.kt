package util

actual inline fun debugLog(lazyMessage: () -> String) {
    // TODO review jvm logger
    println(lazyMessage())
}
