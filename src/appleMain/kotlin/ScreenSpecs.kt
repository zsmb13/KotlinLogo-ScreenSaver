import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.NSView

@OptIn(ExperimentalForeignApi::class)
fun ScreenSpecs(view: NSView): ScreenSpecs {
    val screenWidth = view.frame.useContents { this.size.width }
    val screenHeight = view.frame.useContents { this.size.height }
    return ScreenSpecs(
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        // Magic numbers 🪄✨
        pxScale = ((screenWidth / 1728) + (screenHeight / 1117)) / 2,
    )
}
