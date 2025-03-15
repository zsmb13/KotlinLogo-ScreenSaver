package compose

import ScreenSaverImpl
import ScreenSpecs
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MyComposeWindow
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
import kotlin.math.pow


private fun readPrefValues(): PrefValues = PrefValues(
    logoSize = GlobalPreferences.LOGO_SIZE,
    logoSet = GlobalPreferences.LOGO_SET,
    logoCount = GlobalPreferences.LOGO_COUNT,
    speed = GlobalPreferences.SPEED,
)

@OptIn(ExperimentalForeignApi::class)
class ComposeScreenSaverView(
    private val screenSaverView: NSView,
    private val show: Boolean,
) : ScreenSaverImpl {
    var composeView: NSView? = null
    var observer: NSObjectProtocol? = null

    var mcw: MyComposeWindow? = null

    init {
        val specs = ScreenSpecs(screenSaverView)
        var prefs by mutableStateOf(readPrefValues())

        val debouncer = Debouncer()
        observer = NSNotificationCenter.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debouncer.execute {
                    prefs = readPrefValues()
                }
            }

         mcw = MyComposeWindow(
            size = DpSize(specs.screenWidth.dp, specs.screenHeight.dp),
             show = show,
        ) {
            if (composeView == null && window.contentView != null) {
                composeView = window.contentView
                debugLog { "Set composeView: $composeView" }
            } else {
                debugLog { "Not setting composeView: $composeView, ${window.contentView}" }
            }

            val density = LocalDensity.current
            val imageSet = remember(prefs) {
                imageSets[prefs.logoSet]
            }
            val imgLoader = remember(prefs, density, specs) {
                ComposeImageLoader(
                    density = density,
                    targetArea = (prefs.logoSize * specs.pxScale).pow(2).toFloat(),
                )
            }
            ScreenSaverContent(prefs, imageSet, imgLoader, specs)
        }

        val cv = composeView
        if (cv != null) {
            debugLog { "Attaching $cv to screen saver" }
            cv.frame = NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight)
            screenSaverView.addSubview(cv)
            debugLog { "Attached $cv to screen saver" }
        } else {
            debugLog { "Can't attach to screen saver" }
        }
    }

    override val view: NSView
        get() = composeView!!

    override fun dispose() {
        mcw?.dispose()
        mcw?.window?.close()
        mcw  = null

        observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        observer = null

        composeView?.removeFromSuperview()
        composeView = null
    }

    override fun animateOneFrame() = Unit
}
