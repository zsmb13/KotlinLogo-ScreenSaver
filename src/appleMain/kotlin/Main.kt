import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import compose.ComposeImageLoader
import compose.PrefValues
import compose.ScreenSaverContent
import imagesets.CustomFolderImageSet
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.NSApp
import platform.AppKit.NSApplication
import kotlin.math.pow

@OptIn(ExperimentalForeignApi::class)
fun main() {
    NSApplication.sharedApplication()

    Window("KotlinLogo macOS native") {
        val specs = remember {
            ScreenSpecs(
                screenWidth = this.window.contentLayoutRect.useContents { this.size.width },
                screenHeight = this.window.contentLayoutRect.useContents { this.size.height },
                pxScale = 2.0,
            )
        }

        var prefs by remember {
            mutableStateOf(
                PrefValues(
                    logoSize = 50,
                    logoSet = -1, // unused
                    logoCount = 1,
                    speed = 10,
                )
            )
        }

        ScreenSaverContent(
            prefs = prefs,
            imageSet = CustomFolderImageSet("/Users/zsmb/screensaver-images")!!,
            imgLoader = ComposeImageLoader(
                LocalDensity.current,
                targetArea = (prefs.logoSize * specs.pxScale).pow(2).toFloat()
            ),
            specs = specs,
            onClick = {
                val newpref = prefs.copy(logoSize = prefs.logoSize + 10)
                println("new pref is $newpref")
                prefs = newpref
                println("new pref set!")
            }
        )
    }

    NSApp?.run()
}
