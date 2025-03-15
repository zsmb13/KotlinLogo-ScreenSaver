import appkit.AppKitScreenSaverView
import compose.ComposeScreenSaverView
import config.GlobalPreferences
import config.KotlinLogosPrefController
import config.UserDefaultsPreferences.APP_ID
import platform.AppKit.NSWindow
import platform.Foundation.NSBundle
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.ScreenSaver.ScreenSaverView
import util.Debouncer
import util.debugLog

fun create(): KotlinScreenSaverView =
    DynamicScreenSaverView().also {
        debugLog { "Created DynamicScreenSaverView" }
    }

abstract class KotlinScreenSaverView {
    protected lateinit var view: ScreenSaverView
        private set

    protected lateinit var bundle: NSBundle
        private set

    protected var isPreview = false
        private set

    open fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        this.view = screenSaverView
        this.bundle = NSBundle.bundleWithIdentifier(APP_ID)!!
        this.isPreview = isPreview
        screenSaverView.animationTimeInterval = 1 / 60.0
    }

    abstract fun animateOneFrame()

    open val configureSheet: NSWindow? = null

    open fun dispose() {}
}

class DynamicScreenSaverView : KotlinScreenSaverView() {
    private var activeImpl: ScreenSaverImpl? = null

    private var useCompose = GlobalPreferences.USE_COMPOSE
    private val debouncer = Debouncer(500)

    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        super.init(screenSaverView, isPreview)
        setupUserDefaultsObserver()
        switchImplementation()
    }

    override fun animateOneFrame() {
        activeImpl?.animateOneFrame()
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
            ComposeScreenSaverView(view, show = true)
        } else {
            debugLog { "Switching to AppKit implementation" }
            AppKitScreenSaverView(view)
        }
//        activeImpl?.init(view, isPreview)
    }

    private fun setupUserDefaultsObserver() {
        NSNotificationCenter.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debugLog { "Pref notification in DynamicScreenSaverView" }
                debouncer.execute {
                    switchImplementation()
                }
            }
    }
}
