package appkit

import ScreenSaverImpl
import ScreenSpecs
import config.GlobalPreferences
import imagesets.imageSets
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSView
import platform.Foundation.NSMakeRect
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.darwin.NSObjectProtocol
import util.Debouncer
import util.debugLog

@OptIn(ExperimentalForeignApi::class)
class AppKitScreenSaverView(
    private val screenSaverView: NSView,
) : ScreenSaverImpl {
    val specs = ScreenSpecs(screenSaverView)

    override val view: NSView = run {
        NSView(
            NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight)
        )
    }

    private var logos: List<BouncingLogo> = emptyList()

    init {
        debugLog { "LogoScreenSaverView inited ($this)" }
        setupUserDefaultsObserver()
        initLogos()
        screenSaverView.addSubview(view)
    }

    override fun animateOneFrame() {
        logos.forEach(BouncingLogo::animateFrame)
        logos.forEach(BouncingLogo::draw)
    }

    private fun initLogos() {
        disposeLogos()

        val imageLoader = ImageLoader(specs)
        logos = List(GlobalPreferences.LOGO_COUNT) {
            BouncingLogo(
                view = view,
                imageSet = imageSets[GlobalPreferences.LOGO_SET],
                specs = specs,
                imageLoader = imageLoader,
            )
        }
        debugLog { "Created ${logos.size} new logos" }
    }

    private val debouncer = Debouncer()

    private fun disposeLogos() {
        val oldLogos = logos
        logos = emptyList()
        oldLogos.forEach(BouncingLogo::dispose)
        debugLog { "Disposed ${oldLogos.size} old logos" }
    }

    override fun dispose() {
        removeUserDefaultsObserver()
        disposeLogos()
        view.removeFromSuperview()
    }

    var observer: NSObjectProtocol? = null

    private fun setupUserDefaultsObserver() {
        observer = NSNotificationCenter.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debugLog { "Pref notification in AppKitScreenSaverView" }
                debouncer.execute { initLogos() }
            }
    }

    private fun removeUserDefaultsObserver() {
        observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        observer = null
    }
}
