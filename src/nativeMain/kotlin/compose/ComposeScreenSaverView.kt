package compose

import KotlinScreenSaverView
import ScreenSpecs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import config.KotlinLogosPrefController
import config.Preferences
import imagesets.imageSets
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSView
import platform.AppKit.NSWindow
import platform.Foundation.NSMakeRect
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.ScreenSaver.ScreenSaverView
import util.Debouncer
import util.ispreview
import kotlin.math.pow

class ComposeScreenSaverView : KotlinScreenSaverView() {
    private val preferencesController by lazy { KotlinLogosPrefController() }
    override val configureSheet: NSWindow?
        get() = preferencesController.window

    data class PrefValues(
        val logoSize: Int,
        val logoSet: Int,
        val logoCount: Int,
        val speed: Int,
    )

    @OptIn(ExperimentalForeignApi::class)
    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        super.init(screenSaverView, isPreview)
        ispreview = isPreview

        lateinit var composeView: NSView

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
            composeView = window.contentView!!
            val imageSet = remember(prefs) { imageSets[prefs.logoSet] }

            val density = LocalDensity.current
            val imgLoader = remember(prefs, density) {
                ImageLoader(
                    density = density,
                    targetArea = (prefs.logoSize * specs.pxScale).pow(2).toFloat(),
                )
            }

            val screenW = remember { specs.screenWidth.toFloat() * density.density }
            val screenH = remember { specs.screenHeight.toFloat() * density.density }

            Box(
                Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.TopStart,
            ) {
                repeat(prefs.logoCount) {
                    BouncingLogo(
                        imageSet = imageSet,
                        imgLoader = imgLoader,
                        screenW = screenW,
                        screenH = screenH,
                        pxScale = specs.pxScale,
                        baseSpeed = prefs.speed,
                        logoSize = prefs.logoSize,
                    )
                }
            }
        }

        composeView.frame = NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight)
        screenSaverView.addSubview(composeView)
    }

    private fun readPrefValues(): PrefValues = PrefValues(
        logoSize = Preferences.LOGO_SIZE,
        logoSet = Preferences.LOGO_SET,
        logoCount = Preferences.LOGO_COUNT,
        speed = Preferences.SPEED,
    )

    override fun animateOneFrame() = Unit
}
