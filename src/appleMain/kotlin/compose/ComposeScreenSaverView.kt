package compose

import ComposeContentHolder
import ScreenSaverImpl
import ScreenSpecs
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import config.GlobalPreferences
import imagesets.imageSets
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSView
import platform.AppKit.NSApplication
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
) : ScreenSaverImpl {
    private var composeView: NSView? = null

    private var composeWindow: ComposeContentHolder? = null

    private var prefs by mutableStateOf(readPrefValues())

    override fun start() {
        // TODO REMOVE LATER
        GlobalPreferences.CUSTOM_FOLDER = "/Users/zsmb/screensaver-images"

        debugLog { "initing ComposeScreenSaverView" }

        val specs = ScreenSpecs(screenSaverView)

        debugLog { "initing ComposeScreenSaverView 2" }
        debugLog { "specs are ${specs.screenWidth}x${specs.screenHeight}, scale ${specs.pxScale}" }

        debugLog { "initing ComposeScreenSaverView 3" }
        val ww = screenSaverView.window
        debugLog { "window is $ww" }

        NSApplication.sharedApplication.windows.forEach { window ->
            debugLog { "NSApplication has window $window" }
        }
        debugLog { "before density" }
        val dens = ww!!.backingScaleFactor.toFloat()
        debugLog { "after density" }
        debugLog { "density is $dens" }
        val composeContentHolder = ComposeContentHolder(
            density = dens,
            size = DpSize(specs.screenWidth.dp, specs.screenHeight.dp),
        )
        composeWindow = composeContentHolder

        screenSaverView.addSubview(composeContentHolder.view)

        composeView = composeContentHolder.view

        debugLog { "COMPOSE_ONCE: setContent" }
        composeContentHolder.setContent {
            // Don't touch this, removing it leads to a compilation error somehow
            // https://youtrack.jetbrains.com/issue/KT-76037/
//            observer

            val density = LocalDensity.current
            val imageSet = remember(prefs) {
//                imageSets[prefs.logoSet]
                imageSets[2]
            }
            val imgLoader = remember(prefs, density, specs) {
                ComposeImageLoader(
                    density = density,
                    targetArea = (prefs.logoSize * specs.pxScale).pow(2).toFloat(),
                )
            }
            debugLog { "COMPOSE_ONCE: ScreenSaverContent call" }
            ScreenSaverContent(prefs, imageSet, imgLoader, specs)
        }
    }

    override fun dispose() {
        debugLog { "disposing ComposeScreenSaverView" }
        composeWindow?.dispose()

        composeView = null
        composeWindow = null
    }

    override fun prefsChanged() {
        prefs = readPrefValues()
    }

    override fun animateOneFrame() = Unit
}
