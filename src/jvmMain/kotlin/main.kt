import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import compose.ComposeFolderImageLoader
import compose.ScreenSaverContent
import imagesets.CustomFolderImageSet
import imagesets.UImageLoader
import imagesets.add
import org.jetbrains.compose.reload.DevelopmentEntryPoint


fun main() {
    val customFolder = System.getProperty("customfolder")
    val folder = if (customFolder.isNullOrBlank()) {
        println("Warning: custom folder not set. You can specify a custom folder with  -Pcustomfolder=\"/path/to/your/images\"")
        "./sampleImages"
    } else {
        customFolder
    }

    application {
        val windowState = rememberWindowState(width = 800.dp, height = 600.dp)
        Window(
            title = "KotlinLogo JVM",
            onCloseRequest = ::exitApplication,
            state = windowState,
            alwaysOnTop = true,
        ) {
            DevelopmentEntryPoint {
                val specs = remember(windowState.size) {
                    ScreenSpecs(
                        screenWidth = windowState.size.width.value.toDouble(),
                        screenHeight = windowState.size.height.value.toDouble() - window.insets.top,
                        pxScale = 1.0,
                    )
                }

                val paramProvider = remember {
                    object : ParamProvider {
                        override val params: ScreenSaverParams = ScreenSaverParams(
                            logoSize = 100,
                            logoCount = 5,
                            speed = 20,
                            imageSet = CustomFolderImageSet(folder)
                                ?: throw IllegalStateException("Couldn't load folder \"$folder\""),
                            useCompose = true,
                            demoMode = true,
                            debugMode = false,
                        )
                        override var callback: (ScreenSaverParams) -> Unit = {}
                    }
                }

                val uImageLoader = remember(paramProvider, specs) {
                    UImageLoader(paramProvider, specs).apply {
                        add<Painter>(ComposeFolderImageLoader())
                    }
                }

                ScreenSaverContent(
                    params = paramProvider.params,
                    imgLoader = uImageLoader,
                    specs = specs,
                )
            }
        }
    }
}
