import appkit.AppKitScreenSaverView
import compose.ComposeScreenSaverView
import config.GlobalPreferences
import config.KotlinLogosPrefController
import platform.AppKit.NSWindow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.ScreenSaver.ScreenSaverView
import util.Debouncer
import util.debugLog

class DynamicScreenSaverView : KotlinScreenSaverView() {
    private var activeImpl: ScreenSaverImpl? = null

    private var useCompose = GlobalPreferences.USE_COMPOSE
    private val debouncer = Debouncer(500)

    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        super.init(screenSaverView, isPreview)
        debugLog { "Created DynamicScreenSaverView" }
        debugLog { "screenSaverView=$screenSaverView" }
        debugLog { "screenSaverView.window=${screenSaverView.window}" }
        setupUserDefaultsObserver()
        switchImplementation()
    }

    override fun animateOneFrame() {
        activeImpl?.animateOneFrame()
    }

    var isAnimating = false

    override fun startAnimation() {
        debugLog { "START ANIMATION CALLBACK, screensaver is $view, its window is ${view.window}" }
        isAnimating = true
        activeImpl?.start()
    }

    private val preferencesController by lazy { KotlinLogosPrefController() }
    override val configureSheet: NSWindow?
        get() = preferencesController.window

    private fun switchImplementation() {
        val useCompose = GlobalPreferences.USE_COMPOSE
        if (this.useCompose == useCompose && activeImpl != null) {
            return
        }
        this.useCompose = useCompose

        val disposing = activeImpl
        activeImpl = null

        // Clean up old implementation if it exists
        disposing?.let { impl ->
            debugLog { "Cleaning up old implementation: ${impl::class.simpleName}" }
            // Additional cleanup if needed
            impl.dispose()
        }

        // Create and initialize new implementation
        activeImpl = if (useCompose) {
            debugLog { "Switching to Compose implementation" }
            ComposeScreenSaverView(view)
        } else {
            debugLog { "Switching to AppKit implementation" }
            AppKitScreenSaverView(view)
        }
        if (isAnimating) {
            debugLog { "Start 1, activeImpl $activeImpl" }
            activeImpl?.start()
            debugLog { "Start 2, activeImpl $activeImpl" }
        } else {
            debugLog { "Start failed, activeImpl $activeImpl" }
        }
    }

    private fun setupUserDefaultsObserver() {
        NSNotificationCenter.Companion.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debugLog { "Pref notification in DynamicScreenSaverView" }
                debouncer.execute {
                    switchImplementation()
                }
            }
    }
}