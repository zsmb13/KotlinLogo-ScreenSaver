@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSRect
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.ScreenSaver.ScreenSaverView

class LogoScreenSaverView : KotlinScreenSaverView() {
    private var logos: List<BouncingLogo> = emptyList()

    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean, bundle: NSBundle) {
        super.init(screenSaverView, isPreview, bundle)
        screenSaverView.animationTimeInterval = 1 / 60.0
        setupUserDefaultsObserver()
        initLogos()
    }

    override fun draw(rect: CPointer<NSRect>) {
        logos.forEach(BouncingLogo::draw)
    }

    override fun animateOneFrame() {
        logos.forEach(BouncingLogo::animateOneFrame)
        view.setNeedsDisplayInRect(view.frame)
    }

    private val debouncer = Debouncer(500)

    private fun setupUserDefaultsObserver() {
        NSNotificationCenter.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debouncer.execute { initLogos() }
            }
    }

    private fun initLogos() {
        logos.forEach(BouncingLogo::dispose)
        logos = List(Preferences.LOGO_COUNT) { BouncingLogo(view, bundle, imageSets[Preferences.LOGO_SET].images()) }
    }
}

