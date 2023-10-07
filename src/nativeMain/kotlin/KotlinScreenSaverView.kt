@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSWindow
import platform.Foundation.NSBundle
import platform.Foundation.NSRect
import platform.ScreenSaver.ScreenSaverView
import util.debugLog

fun create(): KotlinScreenSaverView = LogoScreenSaverView().also {
    debugLog { "Created KotlinScreenSaverView" }
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
        this.bundle = NSBundle.bundleWithIdentifier("co.zsmb.KotlinLogos")!!
        this.isPreview = isPreview
    }

    abstract fun draw(rect: CPointer<NSRect>)
    abstract fun animateOneFrame()

    open val configureSheet: NSWindow? = null
}
