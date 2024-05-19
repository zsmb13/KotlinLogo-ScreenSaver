import config.Preferences
import imagesets.ImageSet
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSBundle
import util.debugLog
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalForeignApi::class)
class ImageLoader(
    private val specs: ScreenSpecs,
    private val imageCache: MutableMap<String, ImageWithDimensions> = mutableMapOf(),
) {
    /**
     * Calculates logo width and heigh amounts so that their ratio matches
     * the image ratio and their total area is LOGO_AREA.
     */
    fun loadImage(imageSet: ImageSet, index: Int): ImageWithDimensions = imageCache.getOrPut(
        key = imageSet.images[index],
        defaultValue = {
            val img = imageSet.load(imageSet.images[index])
            debugLog { "Loaded $index from $imageSet: $img"}

            val (w, h) = img.size.useContents { width to height }
            val area = (Preferences.LOGO_SIZE.toDouble() * specs.pxScale).pow(2)
            val logoHeight = sqrt(area / (w / h))
            val logoWidth = area / logoHeight

            ImageWithDimensions(nsImage = img, logoWidth = logoWidth, logoHeight = logoHeight)
        }
    )
}
