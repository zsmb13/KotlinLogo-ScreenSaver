package compose

import ScreenSaverParams
import ScreenSpecs
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import imagesets.UImageLoader
import util.debugLog

@Composable
fun ScreenSaverContent(
    params: ScreenSaverParams,
    imgLoader: UImageLoader,
    specs: ScreenSpecs,
    onClick: () -> Unit = {},
) {
    val density = LocalDensity.current
    val screenW = remember(specs, density) { specs.screenWidth.toFloat() * density.density }
    val screenH = remember(specs, density) { specs.screenHeight.toFloat() * density.density }

    debugLog { "DENSITY IS $density; ${density.density}" }

    Box(
        Modifier.fillMaxSize()
            .clickable(onClick = onClick)
            .background(if (params.debugMode) Color.DarkGray else Color.Black),
        contentAlignment = Alignment.TopStart,
    ) {
        repeat(params.logoCount) {
            BouncingLogo(
                imageSet = params.imageSet,
                imgLoader = imgLoader,
                screenW = screenW,
                screenH = screenH,
                pxScale = specs.pxScale,
                baseSpeed = params.speed,
                logoSize = params.logoSize,
                debug = params.debugMode,
            )
        }
        if (params.demoMode) {
            DemoContent(specs)
        }
    }
}

@Composable
expect fun BoxScope.DemoContent(specs: ScreenSpecs)
