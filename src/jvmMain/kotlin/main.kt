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
    var prefs by mutableStateOf(
        PrefValues(
            logoSize = 40,
            logoSet = -1, // unused
            logoCount = 5,
            speed = 15,
        )
    )

    val windowWidth = 800.0
    val windowHeight = 600.0

    application {
        Window(
            title = "KotlinLogo JVM",
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(width = windowWidth.dp, height = windowHeight.dp),
            resizable = false,
            alwaysOnTop = true,
        ) {
            DevelopmentEntryPoint {
                val specs = remember {
                    ScreenSpecs(
                        screenWidth = windowWidth,
                        screenHeight = windowHeight - window.insets.top,
                        pxScale = 1.0,
                    )
                }
                val density = LocalDensity.current
                val composeImageLoader = remember {
                    ComposeImageLoader(
                        density = density,
                        targetArea = (prefs.logoSize * specs.pxScale).pow(2).toFloat()
                    )
                }
                ScreenSaverContent(
                    prefs = prefs,
                    imageSet = CustomFolderImageSet("/Users/zsmb/screensaver-images")!!,
                    imgLoader = composeImageLoader,
                    specs = specs,
                    onClick = {
                        prefs = prefs.copy(logoCount = prefs.logoCount + 1)
                    }
                )
            }
        }
    }
}
