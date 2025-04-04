package compose

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import imagesets.Adjuster
import imagesets.CustomFolderImageSet
import imagesets.ImageLoader
import imagesets.ImageSet
import imagesets.LoadedImage
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.decodeToSvgPainter

class ComposeFolderImageLoader : ImageLoader<Painter> {
    @OptIn(ExperimentalResourceApi::class)
    override fun loadImage(
        imageSet: ImageSet,
        index: Int,
        adjustSize: Adjuster,
    ): LoadedImage<Painter>? {
        if (imageSet !is CustomFolderImageSet) return null
        val imagePath = imageSet.images[index].substringAfter("file://")
        val bytes = SystemFileSystem.source(Path(imagePath))
            .buffered()
            .use(Source::readByteArray)
        val painter = if (imagePath.endsWith("svg")) {
            bytes.decodeToSvgPainter(Density(1f)) // density is canceled out by division below
        } else {
            BitmapPainter(bytes.decodeToImageBitmap())
        }

        val (w, h) = adjustSize(painter.intrinsicSize.width / painter.intrinsicSize.height)
        return LoadedImage(
            image = painter,
            width = w,
            height = h,
        )
    }
}
