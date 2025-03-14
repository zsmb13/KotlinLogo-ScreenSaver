package appkit

import KotlinScreenSaverView
import ScreenSpecs
import config.GlobalPreferences
import config.KotlinLogosPrefController
import imagesets.imageSets
import platform.AppKit.NSWindow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.ScreenSaver.ScreenSaverView
import util.Debouncer
import util.debugLog

class LogoScreenSaverView : KotlinScreenSaverView() {
    private val preferencesController by lazy { KotlinLogosPrefController() }
    override val configureSheet: NSWindow?
        get() = preferencesController.window

    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        super.init(screenSaverView, isPreview)
        debugLog { "LogoScreenSaverView inited, isPreview: $isPreview" }
        screenSaverView.animationTimeInterval = 1 / 60.0
        setupUserDefaultsObserver()
        initLogos()
    }

    private var logos: List<BouncingLogo> = emptyList()

    override fun animateOneFrame() {
        logos.forEach(BouncingLogo::animateFrame)
        logos.forEach(BouncingLogo::draw)
    }

    private fun initLogos() {
        logos.forEach(BouncingLogo::dispose)

        val specs = ScreenSpecs(view)
        val imageLoader = ImageLoader(specs)
        logos = List(GlobalPreferences.LOGO_COUNT) {
            BouncingLogo(
                view = view,
                imageSet = imageSets[GlobalPreferences.LOGO_SET],
                specs = specs,
                imageLoader = imageLoader,
            )
        }
    }

    private val debouncer = Debouncer(delayMs = 500)

    private fun setupUserDefaultsObserver() {
        NSNotificationCenter.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debouncer.execute { initLogos() }
            }
    }
}
