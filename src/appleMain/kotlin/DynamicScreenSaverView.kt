import appkit.AppKitScreenSaverView
import compose.ComposeScreenSaverView
import config.KotlinLogosPrefController
import config.UserDefaultsPreferences
import imagesets.ImageSetRepo
import imagesets.UImageLoader
import platform.AppKit.NSView
import platform.AppKit.NSWindow
import util.debugLog

class DynamicScreenSaverView(
    val paramProvider: ParamProvider,
    val imageSetRepo: ImageSetRepo,
    val imageLoader: UImageLoader,
) : KotlinScreenSaverView() {
    private var activeImpl: ScreenSaverImpl? = null

    override fun init(screenSaverView: NSView, isPreview: Boolean) {
        super.init(screenSaverView, isPreview)
        debugLog { "Created DynamicScreenSaverView" }

        switchImplementation(paramProvider.params)

        paramProvider.addCallback { newParams ->
            debugLog { "Param callback with $newParams" }
            switchImplementation(newParams)
            activeImpl?.prefsChanged(newParams)
        }
    }

    override fun animateOneFrame() {
        activeImpl?.animateOneFrame()
    }

    var isAnimating = false

    override fun startAnimation() {
        if (isAnimating) {
            debugLog { "ignoring START ANIM" }
            return
        }
        debugLog { "START ANIMATION CALLBACK, screensaver is $view, its window is ${view.window}" }
        isAnimating = true
        activeImpl?.start(paramProvider.params)
    }

    private val preferencesController by lazy { KotlinLogosPrefController(UserDefaultsPreferences, imageSetRepo) }
    override val configureSheet: NSWindow?
        get() = preferencesController.window

    private fun switchImplementation(params: ScreenSaverParams) {
        val disposing = activeImpl
        activeImpl = null

        // Clean up old implementation if it exists
        disposing?.let { impl ->
            debugLog { "Cleaning up old implementation: ${impl::class.simpleName}" }
            // Additional cleanup if needed
            impl.dispose()
        }

        // Create and initialize new implementation
        activeImpl = if (params.useCompose) {
            debugLog { "Switching to Compose implementation" }
            ComposeScreenSaverView(view, imageLoader)
        } else {
            debugLog { "Switching to AppKit implementation" }
            AppKitScreenSaverView(view, imageLoader)
        }
        if (isAnimating) {
            activeImpl?.start(params)
        }
    }
}
