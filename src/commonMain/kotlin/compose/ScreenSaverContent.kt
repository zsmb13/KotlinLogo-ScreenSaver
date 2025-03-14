package compose

import ScreenSpecs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import imagesets.ImageSet

@Composable
fun ScreenSaverContent(
    prefs: PrefValues,
    imageSet: ImageSet,
    imgLoader: ImageLoader,
    specs: ScreenSpecs
) {
    val density = LocalDensity.current
    val screenW = remember { specs.screenWidth.toFloat() * density.density }
    val screenH = remember { specs.screenHeight.toFloat() * density.density }

    Box(
        Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.TopStart,
    ) {
        repeat(prefs.logoCount) {
            BouncingLogo(
                imageSet = imageSet,
                imgLoader = imgLoader,
                screenW = screenW,
                screenH = screenH,
                pxScale = specs.pxScale,
                baseSpeed = prefs.speed,
                logoSize = prefs.logoSize,
            )
        }
    }
}
