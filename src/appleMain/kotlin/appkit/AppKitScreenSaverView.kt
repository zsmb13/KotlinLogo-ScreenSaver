package appkit

import ScreenSaverImpl
import ScreenSaverParams
import ScreenSpecs
import imagesets.UImageLoader
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSColor
import platform.AppKit.NSFont
import platform.AppKit.NSImage
import platform.AppKit.NSImageScaleProportionallyUpOrDown
import platform.AppKit.NSImageView
import platform.AppKit.NSTextView
import platform.AppKit.NSView
import platform.Foundation.NSMakeRect
import util.debugLog

@OptIn(ExperimentalForeignApi::class)
class AppKitScreenSaverView(
    private val screenSaverView: NSView,
    private val imageLoader: UImageLoader,
) : ScreenSaverImpl {

    private var view: NSView? = null

    private var logos: List<BouncingLogo> = emptyList()

    private val specs = ScreenSpecs(screenSaverView)

    private var demoView1: NSView? = null
    private var demoView2: NSView? = null

    override fun start(params: ScreenSaverParams) {
        debugLog { "LogoScreenSaverView initing ($this)" }

        view?.removeFromSuperview()
        val nsView = NSView(NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight))
        nsView.wantsLayer = true
        nsView.layer?.backgroundColor = NSColor.blackColor.CGColor
        view = nsView
        screenSaverView.addSubview(nsView)
        debugLog { "Created and attached NSView to screenSaverView, $nsView, ${specs.screenWidth}x${specs.screenHeight}" }

        initLogos(params)
    }

    override fun animateOneFrame() {
        logos.forEach(BouncingLogo::animateFrame)
        logos.forEach(BouncingLogo::draw)
    }

    private fun initLogos(params: ScreenSaverParams) {
        disposeLogos()

        val view = view
        if (view == null) {
            debugLog { "View is null, can't initialize logos" }
            return
        }

        if (params.demoMode) {
            createDemoUI(view)
        } else {
            demoView1?.removeFromSuperview()
            demoView2?.removeFromSuperview()
            demoView1 = null
            demoView2 = null
        }

        logos = List(params.logoCount) {
            BouncingLogo(
                view = view,
                imageSet = params.imageSet,
                specs = specs,
                baseSpeed = params.speed,
                baseSize = params.logoSize,
                imageLoader = imageLoader,
            )
        }
    }

    private fun createDemoUI(view: NSView) {
        if (demoView1 == null) {
            demoView1 = createDemoImageView().also { view.addSubview(it) }
        }
        if (demoView2 == null) {
            demoView2 = createDemoTextView().also { view.addSubview(it) }
        }
    }

    private fun createDemoImageView(): NSImageView {
        return NSImageView().apply {
            image = NSImage.imageWithSystemSymbolName("macwindow", null)
            imageScaling = NSImageScaleProportionallyUpOrDown
            frame = NSMakeRect(
                14.0 * specs.pxScale,
                14.0 * specs.pxScale,
                70.0 * specs.pxScale,
                70.0 * specs.pxScale
            )
        }
    }

    private fun createDemoTextView(): NSTextView {
        return NSTextView().apply {
            frame = NSMakeRect(
                84.0 * specs.pxScale,
                24.0 * specs.pxScale,
                400.0 * specs.pxScale,
                35.0 * specs.pxScale
            )
            string = "Running on AppKit"
            font = NSFont.systemFontOfSize(35.0 * specs.pxScale)
            backgroundColor = null
        }
    }

    private fun disposeLogos() {
        val oldLogos = logos
        logos = emptyList()
        oldLogos.forEach(BouncingLogo::dispose)
    }

    override fun dispose() {
        disposeLogos()
        view?.removeFromSuperview()
        view = null
    }

    override fun prefsChanged(params: ScreenSaverParams) {
        initLogos(params)
    }
}
