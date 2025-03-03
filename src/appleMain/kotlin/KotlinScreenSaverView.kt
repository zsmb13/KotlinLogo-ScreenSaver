import appkit.NSImageLoader
import compose.ComposeAssetImageLoader
import compose.ComposeFolderImageLoader
import config.UserDefaultsPreferences
import imagesets.RealImageSetRepo
import imagesets.UImageLoader
import imagesets.add
import platform.AppKit.NSView
import platform.AppKit.NSWindow
import util.debugLog

fun create(
    screenSaverView: NSView,
    isPreview: Boolean,
): KotlinScreenSaverView {
    debugLog { "Creating KotlinScreenSaverView" }

    val prefStorage = UserDefaultsPreferences
    val imgSetRepo = RealImageSetRepo(prefStorage)
    val prefParamProvider = PrefParamProvider(prefStorage, imgSetRepo)
    val screenSpecs = ScreenSpecs(screenSaverView)
    val imgLoader = UImageLoader(prefParamProvider, screenSpecs).apply {
        add(ComposeFolderImageLoader())
        add(ComposeAssetImageLoader())
        add(NSImageLoader())
    }

    return DynamicScreenSaverView(prefParamProvider, imgSetRepo, imgLoader)
        .also { it.init(screenSaverView, isPreview) }
        .also { debugLog { "Created KotlinScreenSaverView" } }
}

abstract class KotlinScreenSaverView {
    protected lateinit var view: NSView
        private set

    protected var isPreview = false
        private set

    open fun init(screenSaverView: NSView, isPreview: Boolean) {
        this.view = screenSaverView
        this.isPreview = isPreview
    }

    abstract fun animateOneFrame()

    abstract fun startAnimation()

    open val configureSheet: NSWindow? = null
}

