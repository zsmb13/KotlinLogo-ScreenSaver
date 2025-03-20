import config.UserDefaultsPreferences.APP_ID
import platform.AppKit.NSWindow
import platform.Foundation.NSBundle
import platform.ScreenSaver.ScreenSaverView
import util.debugLog

fun create(): KotlinScreenSaverView {
//    GlobalPreferences.reset()
    return DynamicScreenSaverView().also {
        debugLog { "Created DynamicScreenSaverView" }
    }
}

abstract class KotlinScreenSaverView {
    protected lateinit var view: ScreenSaverView
        private set

    protected lateinit var bundle: NSBundle
        private set

    protected var isPreview = false
        private set

    open fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        this.view = screenSaverView
        this.bundle = NSBundle.bundleWithIdentifier(APP_ID)!!
        this.isPreview = isPreview
        screenSaverView.animationTimeInterval = 1 / 60.0
    }

    abstract fun animateOneFrame()

    abstract fun startAnimation()

    open val configureSheet: NSWindow? = null

    open fun dispose() {}
}

