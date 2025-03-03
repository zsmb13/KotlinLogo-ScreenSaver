package util

actual inline fun debugLog(lazyMessage: () -> String) {
    println(lazyMessage())
}
