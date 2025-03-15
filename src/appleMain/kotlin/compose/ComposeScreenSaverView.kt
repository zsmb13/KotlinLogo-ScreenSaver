package compose

import KotlinScreenSaverView
import ScreenSpecs
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import config.GlobalPreferences
import config.KotlinLogosPrefController
import imagesets.imageSets
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSView
import platform.AppKit.NSWindow
import platform.Foundation.NSMakeRect
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.ScreenSaver.ScreenSaverView
import util.Debouncer
import kotlin.math.pow


private fun readPrefValues(): PrefValues = PrefValues(
    logoSize = GlobalPreferences.LOGO_SIZE,
    logoSet = GlobalPreferences.LOGO_SET,
    logoCount = GlobalPreferences.LOGO_COUNT,
    speed = GlobalPreferences.SPEED,
)


class ComposeScreenSaverView : KotlinScreenSaverView() {
    private val preferencesController by lazy { KotlinLogosPrefController() }
    override val configureSheet: NSWindow?
        get() = preferencesController.window

    @OptIn(ExperimentalForeignApi::class)
    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        super.init(screenSaverView, isPreview)

        var composeView: NSView? = null

        val specs = ScreenSpecs(screenSaverView)
        var prefs by mutableStateOf(readPrefValues())

        val debouncer = Debouncer(delayMs = 500)
        NSNotificationCenter.defaultCenter
            .addObserverForName(NSUserDefaultsDidChangeNotification, null, null) {
                debouncer.execute {
                    prefs = readPrefValues()
                }
            }

        Window(
            size = DpSize(specs.screenWidth.dp, specs.screenHeight.dp),
        ) {
            if (composeView == null && window.contentView != null) {
                composeView = window.contentView
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
            cv.removeFromSuperview()
            cv.frame = NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight)
            screenSaverView.addSubview(cv)
        }
    }

    override fun animateOneFrame() = Unit
}
