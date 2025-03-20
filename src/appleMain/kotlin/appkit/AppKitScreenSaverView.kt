package appkit

import ScreenSaverImpl
import ScreenSpecs
import config.GlobalPreferences
import imagesets.imageSets
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSView
import platform.Foundation.NSMakeRect
import platform.darwin.NSObjectProtocol
import util.debugLog

@OptIn(ExperimentalForeignApi::class)
class AppKitScreenSaverView(
    private val screenSaverView: NSView,
) : ScreenSaverImpl {
    val specs = ScreenSpecs(screenSaverView)

    var view: NSView? = null

    private var logos: List<BouncingLogo> = emptyList()

    override fun start() {
        debugLog { "LogoScreenSaverView initing ($this)" }

        view?.removeFromSuperview()
        val nsView = NSView(NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight))
        view = nsView
        screenSaverView.addSubview(nsView)
        debugLog { "Created and attached NSView to screenSaverView, $nsView, ${specs.screenWidth}x${specs.screenHeight}" }

        initLogos()
    }

    override fun animateOneFrame() {
//        debugLog { "animateOneFrame in AppKitScreenSaverView, ${logos.size} logos in list, $view, ${view?.window}" }
        logos.forEach(BouncingLogo::animateFrame)
        logos.forEach(BouncingLogo::draw)
    }

    private fun initLogos() {
        disposeLogos()

        val view = view
        if (view == null) {
            debugLog { "View is null, can't initialize logos" }
            return
        }

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

    private fun disposeLogos() {
        val oldLogos = logos
        logos = emptyList()
        oldLogos.forEach(BouncingLogo::dispose)
        debugLog { "Disposed ${oldLogos.size} old logos" }
    }

    override fun dispose() {
        disposeLogos()
        view?.removeFromSuperview()
        view = null
    }

    var observer: NSObjectProtocol? = null

    override fun prefsChanged() {
        initLogos()
    }
}
