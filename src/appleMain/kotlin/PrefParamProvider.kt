import config.PreferenceStorage
import config.RenderMode
import imagesets.ImageSetRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.Foundation.NSDate
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaultsDidChangeNotification
import util.Debouncer
import util.debugLog
import kotlin.time.Duration.Companion.seconds

class PrefParamProvider(
    private val preferenceStorage: PreferenceStorage,
    private val imageSetRepo: ImageSetRepo,
) : ParamProvider {
    override var params: ScreenSaverParams = createParams()
        private set

    private val callbacks = mutableListOf<(ScreenSaverParams) -> Unit>()

    override fun addCallback(cb: (ScreenSaverParams) -> Unit) {
        callbacks += cb
    }

    private fun notifyCallbacks(params: ScreenSaverParams) {
        callbacks.forEach { it(params) }
    }

    private val debouncer = Debouncer(500)

    init {
        setupUserDefaultsObserver()
        checkDemoMode()
    }

    private fun setupUserDefaultsObserver() {
        NSNotificationCenter.Companion.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debugLog { "Pref notification in PrefParamProvider" }

                debouncer.execute {
                    debugLog { "Pref notification after debounce" }

                    val oldParams = params
                    val newParams = createParams()
                    debugLog { "Old params: $oldParams" }
                    debugLog { "New params: $newParams" }

                    if (oldParams != newParams) {
                        debugLog { "Params changed, will notify" }
                        params = createParams()
                        notifyCallbacks(params)
                        checkDemoMode()
                    }
                }
            }
    }

    private fun createParams(): ScreenSaverParams {
        val prefs = preferenceStorage.getPreferences()
        val params = ScreenSaverParams(
            logoSize = prefs.logoSize,
            logoCount = prefs.logoCount,
            speed = prefs.speed,
            useCompose = prefs.renderMode == RenderMode.Compose,
            debugMode = prefs.debugMode,
            demoMode = prefs.renderMode == RenderMode.Demo,
            imageSet = imageSetRepo.getCurrentImageSet(),
        )
        debugLog { "Created ScreenSaverParams: $params" }
        return params
    }

    private var demoJob: Job? = null
    private fun checkDemoMode() {
        debugLog { "DEMODEBUG ${now()}: Checking demo mode" }

        if (!params.demoMode) {
            debugLog { "DEMODEBUG ${now()}: Not in demo mode, cancelling job" }
            demoJob?.cancel()
            demoJob = null
            return
        }

        debugLog { "DEMODEBUG ${now()}: In demo mode, checking job" }

        if (demoJob == null) {
            debugLog { "DEMODEBUG ${now()}: In demo mode, starting new job" }

            demoJob = GlobalScope.launch(Dispatchers.Main) {
                debugLog { "DEMODEBUG ${now()}: Demo job start" }
                while (true) {
                    delay(15.seconds)
                    params = params.copy(useCompose = !params.useCompose)
                    debugLog { "DEMODEBUG ${now()}: Demo job swapped value to ${params.useCompose}" }
                    notifyCallbacks(params)
                }
            }
        } else {
            debugLog { "DEMODEBUG ${now()}: Job already running" }
        }
    }
}

fun now() = NSDate().timeIntervalSinceReferenceDate
