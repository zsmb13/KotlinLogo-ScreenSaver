import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import compose.PrefValues
import compose.ScreenSaverContent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.NSApp
import platform.AppKit.NSApplication
import compose.ImageLoader
import config.GlobalPreferences
import imagesets.imageSets
import kotlin.math.pow

@OptIn(ExperimentalForeignApi::class)
fun main() {
    NSApplication.sharedApplication()

    GlobalPreferences.CUSTOM_FOLDER = "/Users/zsmb/screensaver-images"

    Window("KotlinLogo macOS native") {
        val specs = remember {
            ScreenSpecs(
                screenWidth = this.window.frame.useContents { this.size.width },
                screenHeight = this.window.frame.useContents { this.size.height },
                pxScale = 2.0,
            )
        }

        val prefs = PrefValues(logoSize = 20, logoSet = 2, logoCount = 10, speed = 10)

        ScreenSaverContent(
            prefs = prefs,
            imageSet = imageSets[prefs.logoSet],
            imgLoader = ImageLoader(
                LocalDensity.current,
                targetArea = (prefs.logoSize * specs.pxScale).pow(2).toFloat()
            ),
            specs = specs
        )
    }

    NSApp?.run()
}
