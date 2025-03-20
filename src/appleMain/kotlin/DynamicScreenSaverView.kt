import appkit.AppKitScreenSaverView
import compose.ComposeScreenSaverView
import config.GlobalPreferences
import config.KotlinLogosPrefController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AppKit.NSWindow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.ScreenSaver.ScreenSaverView
import util.Debouncer
import util.debugLog
import kotlin.time.Duration.Companion.seconds

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

        // "demo mode"
//        GlobalScope.launch(Dispatchers.Main) {
//            while (true) {
//                delay(4.seconds)
//                GlobalPreferences.USE_COMPOSE = !useCompose
//                switchImplementation()
//            }
//        }
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
            activeImpl?.start()
        }
    }

    private fun setupUserDefaultsObserver() {
        NSNotificationCenter.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debugLog { "Pref notification in DynamicScreenSaverView" }
                debouncer.execute {
                    switchImplementation()
                    activeImpl?.prefsChanged()
                }
            }
    }
}