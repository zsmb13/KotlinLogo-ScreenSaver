package compose

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import imagesets.ImageSet
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToSvgPainter
import kotlin.math.sqrt

data class LogoWithSizes(
    val painter: Painter,
    val width: Float,
    val height: Float,
)

class ImageLoader(
    private val density: Density,
    private val targetArea: Float,
) {
    private val imageCache: MutableMap<String, LogoWithSizes> = mutableMapOf()

    @OptIn(ExperimentalResourceApi::class)
    fun loadImage(imageSet: ImageSet, index: Int): LogoWithSizes {
        val imagePath = imageSet.images[index]
        return imageCache.getOrPut(
            key = imagePath,
            defaultValue = {
                val painter = SystemFileSystem.source(Path(imagePath.substringAfter("file://")))
                    .buffered()
                    .use(Source::readByteArray)
                    .decodeToSvgPainter(density)
                val logoHeight = sqrt(targetArea / (painter.intrinsicSize.width / painter.intrinsicSize.height)).toFloat()
                val logoWidth = targetArea / logoHeight
                LogoWithSizes(
                    painter = painter,
                    width = logoWidth * density.density,
                    height = logoHeight * density.density,
                )
            }
        )
    }
}
