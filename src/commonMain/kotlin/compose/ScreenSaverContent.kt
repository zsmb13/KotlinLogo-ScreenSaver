package compose

import ScreenSpecs
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import config.DEBUG_MODE
import imagesets.ImageSet

@Composable
fun ScreenSaverContent(
    prefs: PrefValues,
    imageSet: ImageSet,
    imgLoader: ComposeImageLoader,
    specs: ScreenSpecs,
    onClick: () -> Unit = {},
) {
    println("KOTLIN: prefs is $prefs")
    LaunchedEffect(prefs) {
        println("KOTLIN: pref key: $prefs")
    }

    val density = LocalDensity.current
    val screenW = remember { specs.screenWidth.toFloat() * density.density }
    val screenH = remember { specs.screenHeight.toFloat() * density.density }

    Box(
        Modifier.fillMaxSize()
            .clickable(onClick = onClick)
            .background(if (DEBUG_MODE) Color.DarkGray else Color.Black),
        contentAlignment = Alignment.TopStart,
    ) {
        println("KOTLIN: pref count: ${prefs.logoCount}")
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
