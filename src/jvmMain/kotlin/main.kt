import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import compose.ImageLoader
import compose.PrefValues
import compose.ScreenSaverContent
import imagesets.CustomFolderImageSet
import kotlin.math.pow

fun main() {
    val prefs = PrefValues(
        logoSize = 100,
        logoSet = 2,
        logoCount = 10,
        speed = 20,
    )

    application {
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(width = 600.dp, height = 800.dp),
        ) {
            val specs = remember {
                ScreenSpecs(
                    screenWidth = this.window.width.toDouble(),
                    screenHeight = this.window.height.toDouble(),
                    pxScale = 2.0,
                )
            }
            val density = LocalDensity.current
            val imageLoader =remember {
                ImageLoader(
                    density = density,
                    targetArea = (prefs.logoSize * 2.0).pow(2).toFloat()
                )
            }
            ScreenSaverContent(
                prefs = prefs,
                imageSet = CustomFolderImageSet("/Users/zsmb/screensaver-images")!!,
                imgLoader = imageLoader,
                specs = specs,
            )
        }
    }
}
