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
        logoSize = 20,
        logoSet = 2,
        logoCount = 10,
        speed = 20,
    )

    val windowWidth = 800.0
    val windowHeight = 600.0

    application {
        Window(
            title = "KotlinLogo JVM",
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(width = windowWidth.dp, height = windowHeight.dp),
            resizable = false,
        ) {
            val specs = remember {
                ScreenSpecs(
                    screenWidth = windowWidth,
                    screenHeight = windowHeight,
                    pxScale = 1.0,
                )
            }
            val density = LocalDensity.current
            val imageLoader = remember {
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
