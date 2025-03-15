import platform.AppKit.NSView

interface ScreenSaverImpl {
    val view: NSView
    fun animateOneFrame() {}
    fun dispose()
}
