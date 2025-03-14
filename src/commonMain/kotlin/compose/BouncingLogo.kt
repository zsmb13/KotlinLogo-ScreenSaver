package compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import imagesets.ImageSet
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
    baseSpeed: Int,
    logoSize: Int,
) {
    val density = LocalDensity.current.density

    var index by remember(imageSet) { mutableIntStateOf(Random.nextInt(imageSet.size)) }

    val (painter, logoWidth, logoHeight) = remember(imageSet, index) {
        imgLoader.loadImage(imageSet, index)
    }

    var delta by remember(pxScale, baseSpeed) {
        val adjustedSpeed = (pxScale * baseSpeed / 10.0 * Random.nextDouble(0.9, 1.1)).toFloat()
        mutableStateOf(
            Offset(
                adjustedSpeed * if (Random.nextBoolean()) 1 else -1,
                adjustedSpeed * if (Random.nextBoolean()) 1 else -1,
            )
        )
    }

    val animXY = remember(screenW, screenH, logoSize, pxScale) {
        val margin = logoSize * pxScale
        Animatable(
            initialValue = Offset(
                Random.nextDouble(margin, screenW - margin).toFloat(),
                Random.nextDouble(margin, screenH - margin).toFloat()
            ),
            typeConverter = Offset.VectorConverter,
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            val (currentX, currentY) = animXY.value

            val remainingX = if (delta.x > 0) screenW - (currentX + logoWidth / 2) else (currentX - logoWidth / 2)
            val remainingY = if (delta.y > 0) screenH - (currentY + logoHeight / 2) else (currentY - logoHeight / 2)

            val xSmaller = remainingX < remainingY
            val remaining = if (xSmaller) remainingX else remainingY
            val duration = ((remaining / abs(delta.y)) * 60 / density / density).toInt()

            val xTarget = currentX + remaining * if (delta.x > 0) 1 else -1
            val yTarget = currentY + remaining * if (delta.y > 0) 1 else -1

            animXY.animateTo(
                targetValue = Offset(xTarget, yTarget),
                animationSpec = tween(durationMillis = duration, easing = LinearEasing),
            )

            delta = if (xSmaller) {
                delta.copy(x = delta.x * -1)
            } else {
                delta.copy(y = delta.y * -1)
            }
            index = (index + 1) % imageSet.size
        }
    }

    Column(
        Modifier
            .graphicsLayer {
                translationX = animXY.value.x - logoWidth.dp.toPx() / 2
                translationY = animXY.value.y - logoHeight.dp.toPx() / 2
            }
            .size(width = logoWidth.dp, height = logoHeight.dp)
            .background(Color.White)
    ) {
        Image(painter, null, modifier = Modifier.fillMaxSize())
    }
}
