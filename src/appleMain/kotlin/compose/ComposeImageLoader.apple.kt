package compose


import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import appkit.bundle
import imagesets.Adjuster
import imagesets.AssetImageSet
import imagesets.ImageLoader
import imagesets.ImageSet
import imagesets.LoadedImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToSvgPainter
import platform.AppKit.NSDataAsset

@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
class ComposeAssetImageLoader() : ImageLoader<Painter> {
    override fun loadImage(
        imageSet: ImageSet,
        index: Int,
        adjustSize: Adjuster,
    ): LoadedImage<Painter>? {
        if (imageSet !is AssetImageSet) return null

        val data = NSDataAsset(name = imageSet.images[index], bundle).data
        val nativeBytes = requireNotNull(data.bytes)
        val bytes = nativeBytes.readBytes(data.length.toInt())
        val painter = bytes.decodeToSvgPainter(Density(2f)) // density is canceled out by division below
        val (width, height) = adjustSize(painter.intrinsicSize.width / painter.intrinsicSize.height)
        return LoadedImage(
            image = painter,
            width = width,
            height = height,
        )
    }
}
