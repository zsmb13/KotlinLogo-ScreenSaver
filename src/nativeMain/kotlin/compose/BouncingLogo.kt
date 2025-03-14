package compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import imagesets.ImageSet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.abs
import kotlin.random.Random

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BouncingLogo(
    imageSet: ImageSet,
    imgLoader: ImageLoader,
    screenW: Float,
    screenH: Float,
    pxScale: Double,
    speed: Int,
    logoSize: Int,
) {
    val density = LocalDensity.current.density
    var index by remember { mutableIntStateOf(Random.nextInt(imageSet.size)) }

    val (painter, logoWidth, logoHeight) = remember(imageSet, index) {
        imgLoader.loadImage(imageSet, index)
    }

    val speed = remember { (pxScale * speed / 10.0 * Random.nextDouble(0.9, 1.1)).toFloat() }

    var xDelta by remember { mutableStateOf(speed * if (Random.nextBoolean()) 1 else -1) }
    var yDelta by remember { mutableStateOf(speed * if (Random.nextBoolean()) 1 else -1) }

    val margin = remember { logoSize * pxScale }

    val animX = remember { Animatable(Random.nextDouble(margin, screenW - margin).toFloat()) }
    val animY = remember { Animatable(Random.nextDouble(margin, screenH - margin).toFloat()) }

    LaunchedEffect(Unit) {
        while (true) {
            val currentX = animX.value
            val currentY = animY.value

            val remainingX =
                if (xDelta > 0) screenW - (currentX + logoWidth / 2)
                else (currentX - logoWidth / 2)
            val remainingY =
                if (yDelta > 0) screenH - (currentY + logoHeight / 2)
                else (currentY - logoHeight / 2)

            val xSmaller = remainingX < remainingY
            val remaining = if (xSmaller) remainingX else remainingY
            val duration = ((remaining / abs(yDelta)) * 60 / density / density).toInt()
            val xTarget = currentX + remaining * if (xDelta > 0) 1 else -1
            val yTarget = currentY + remaining * if (yDelta > 0) 1 else -1

            awaitAll(
                async {
                    animX.animateTo(
                        xTarget, tween(
                            durationMillis = duration,
                            easing = LinearEasing,
                        )
                    )
                },
                async {
                    animY.animateTo(
                        yTarget, tween(
                            durationMillis = duration,
                            easing = LinearEasing,
                        )
                    )
                }
            )

            if (xSmaller) {
                xDelta *= -1
            } else {
                yDelta *= -1
            }
            index = (index + 1) % imageSet.size
        }
    }

    Column(
        Modifier
            .graphicsLayer {
                translationX = animX.value - logoWidth.dp.toPx() / 2
                translationY = animY.value - logoHeight.dp.toPx() / 2
            }
            .size(width = logoWidth.dp, height = logoHeight.dp)
            .background(Color.White)
    ) {
        Image(painter, null, modifier = Modifier.fillMaxSize())
    }
}
