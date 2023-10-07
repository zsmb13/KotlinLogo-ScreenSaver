@file:OptIn(ExperimentalForeignApi::class)

import config.KotlinLogosPrefController
import config.Preferences
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSWindow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSRect
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.ScreenSaver.ScreenSaverView
import util.Debouncer
import util.debugLog

class LogoScreenSaverView : KotlinScreenSaverView() {
    private val preferencesController by lazy { KotlinLogosPrefController() }
    override val configureSheet: NSWindow?
        get() = preferencesController.window

    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        debugLog { "LogoScreenSaverView inited" }
        super.init(screenSaverView, isPreview)
        screenSaverView.animationTimeInterval = 1 / 60.0
        setupUserDefaultsObserver()
        initLogos()
    }

    private var logos: List<BouncingLogo> = emptyList()

    override fun draw(rect: CPointer<NSRect>) {
        logos.forEach(BouncingLogo::draw)
    }

    override fun animateOneFrame() {
        logos.forEach(BouncingLogo::animateOneFrame)
        view.setNeedsDisplayInRect(view.frame)
    }

    private fun initLogos() {
        logos.forEach(BouncingLogo::dispose)
        logos = List(Preferences.LOGO_COUNT) { BouncingLogo(view, bundle, imageSets[Preferences.LOGO_SET].images()) }
    }

    private val debouncer = Debouncer(delayMs = 500)

    private fun setupUserDefaultsObserver() {
        NSNotificationCenter.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debouncer.execute { initLogos() }
            }
    }
}
