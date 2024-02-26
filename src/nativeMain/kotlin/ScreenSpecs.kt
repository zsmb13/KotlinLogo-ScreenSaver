import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.ScreenSaver.ScreenSaverView

@OptIn(ExperimentalForeignApi::class)
class ScreenSpecs(view: ScreenSaverView) {
    val screenWidth = view.frame.useContents { this.size.width }
    val screenHeight = view.frame.useContents { this.size.height }

    // Magic numbers 🪄✨
    val pxScale = ((screenWidth / 1728) + (screenHeight / 1117)) / 2
}
