package compose

import ComposeContentHolder
import ScreenSaverImpl
import ScreenSaverParams
import ScreenSpecs
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import imagesets.UImageLoader
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSView
import util.debugLog

@OptIn(ExperimentalForeignApi::class)
class ComposeScreenSaverView(
    private val screenSaverView: NSView,
    private val imageLoader: UImageLoader,
) : ScreenSaverImpl {
    private var composeView: NSView? = null

    private var composeWindow: ComposeContentHolder? = null

    private var paramsState by mutableStateOf<ScreenSaverParams?>(null)

    override fun start(params: ScreenSaverParams) {
        debugLog { "initing ComposeScreenSaverView" }
        paramsState = params
        val specs = ScreenSpecs(screenSaverView)
        val composeContentHolder = ComposeContentHolder(
            density = requireNotNull(screenSaverView.window).backingScaleFactor.toFloat(),
            size = DpSize(specs.screenWidth.dp, specs.screenHeight.dp),
        )
        composeWindow = composeContentHolder
        composeView = composeContentHolder.view
        screenSaverView.addSubview(composeContentHolder.view)

        composeContentHolder.setContent {
            ScreenSaverContent(
                params = requireNotNull(paramsState),
                imgLoader = imageLoader,
                specs = specs,
            )
        }
    }

    override fun dispose() {
        debugLog { "disposing ComposeScreenSaverView" }
        composeWindow?.dispose()

        composeView = null
        composeWindow = null
    }

    override fun prefsChanged(params: ScreenSaverParams) {
        paramsState = params
    }

    override fun animateOneFrame() = Unit
}
