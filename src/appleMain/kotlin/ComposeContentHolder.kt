@file:OptIn(InternalComposeUiApi::class, ExperimentalForeignApi::class)

import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.platform.PlatformContext
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import platform.AppKit.NSView
import platform.Foundation.NSMakeRect
import util.debugLog


class ComposeContentHolder(
    private val density: Float,
    size: DpSize = DpSize(800.dp, 600.dp),
) {
    private val skiaLayer = SkiaLayer()
    private val scene = CanvasLayersComposeScene(
        coroutineContext = Dispatchers.Main,
        platformContext = PlatformContext.Empty,
        invalidate = skiaLayer::needRedraw,
    )
    private val renderDelegate = object : SkikoRenderDelegate {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            scene.size = IntSize(width, height)
            scene.render(canvas.asComposeCanvas(), nanoTime)
        }
    }

    val view = object : NSView(
        NSMakeRect(
            x = 0.0,
            y = 0.0,
            w = size.width.value.toDouble(),
            h = size.height.value.toDouble()
        )
    ) {
        override fun wantsUpdateLayer() = true
        override fun acceptsFirstResponder() = true
    }

    private var isDisposed = false

    fun setContent(
        content: @Composable () -> Unit,
    ) {
        skiaLayer.renderDelegate = renderDelegate
        skiaLayer.attachTo(view)
        scene.density = Density(density)
        scene.setContent {
            content()
        }
    }

    fun dispose() {
        if (isDisposed) {
            debugLog { "dispose failed, already disposed!" }
            return
        }
        scene.close()
        skiaLayer.detach()
        view.removeFromSuperview()
        isDisposed = true
    }
}
