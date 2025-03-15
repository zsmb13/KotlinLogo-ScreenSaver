import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import compose.ComposeImageLoader
import compose.PrefValues
import compose.ScreenSaverContent
import imagesets.CustomFolderImageSet
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import kotlin.math.pow

fun main() {
    application {
        val windowState = rememberWindowState(width = 800.dp, height = 600.dp)
        Window(
            title = "KotlinLogo JVM",
            onCloseRequest = ::exitApplication,
            state = windowState,
            alwaysOnTop = true,
        ) {
            DevelopmentEntryPoint {
                var prefs by remember {
                    mutableStateOf(
                        PrefValues(
                            logoSize = 100,
                            logoSet = -1, // unused
                            logoCount = 100,
                            speed = 20,
                        )
                    )
                }
                val specs = remember(windowState.size) {
                    ScreenSpecs(
                        screenWidth = windowState.size.width.value.toDouble(),
                        screenHeight = windowState.size.height.value.toDouble() - window.insets.top,
                        pxScale = 1.0,
                    )
                }
                val density = LocalDensity.current
                val composeImageLoader = remember(density, prefs, specs) {
                    ComposeImageLoader(
                        density = density,
                        targetArea = (prefs.logoSize * specs.pxScale).pow(2).toFloat()
                    )
                }
                val imageSet = remember {
                    CustomFolderImageSet("/Users/zsmb/screensaver-images")!!
                }
                ScreenSaverContent(
                    prefs = prefs,
                    imageSet = imageSet,
                    imgLoader = composeImageLoader,
                    specs = specs,
                )
            }
        }
    }
}
