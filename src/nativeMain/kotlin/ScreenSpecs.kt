import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.ScreenSaver.ScreenSaverView

@OptIn(ExperimentalForeignApi::class)
fun ScreenSpecs(view: ScreenSaverView): ScreenSpecs {
    val screenWidth = view.frame.useContents { this.size.width }
    val screenHeight = view.frame.useContents { this.size.height }
    return ScreenSpecs(
        screenWidth,
        screenHeight,
        // Magic numbers 🪄✨
        ((screenWidth / 1728) + (screenHeight / 1117)) / 2,
    )
}
