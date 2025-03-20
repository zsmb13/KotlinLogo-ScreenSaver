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
import platform.AppKit.NSTrackingActiveAlways
import platform.AppKit.NSTrackingActiveInKeyWindow
import platform.AppKit.NSTrackingArea
import platform.AppKit.NSTrackingAssumeInside
import platform.AppKit.NSTrackingInVisibleRect
import platform.AppKit.NSTrackingMouseEnteredAndExited
import platform.AppKit.NSTrackingMouseMoved
import platform.AppKit.NSView
import platform.AppKit.NSWindow
import platform.Foundation.NSMakeRect


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
        private var trackingArea : NSTrackingArea? = null
        override fun wantsUpdateLayer() = true
        override fun acceptsFirstResponder() = true
        override fun viewWillMoveToWindow(newWindow: NSWindow?) {
            updateTrackingAreas()
        }

        override fun updateTrackingAreas() {
            trackingArea?.let { removeTrackingArea(it) }
            trackingArea = NSTrackingArea(
                rect = bounds,
                options = NSTrackingActiveAlways or
                    NSTrackingMouseEnteredAndExited or
                    NSTrackingMouseMoved or
                    NSTrackingActiveInKeyWindow or
                    NSTrackingAssumeInside or
                    NSTrackingInVisibleRect,
                owner = this, userInfo = null)
            addTrackingArea(trackingArea!!)
        }
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
        if (isDisposed) return
        skiaLayer.detach()
        scene.close()
        view.removeFromSuperview()
        isDisposed = true
    }
}
