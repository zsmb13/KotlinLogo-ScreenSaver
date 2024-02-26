import config.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.imageForResource
import platform.Foundation.NSBundle
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalForeignApi::class)
class ImageLoader(
    private val specs: ScreenSpecs,
    private val bundle: NSBundle,
    private val imageCache: MutableMap<String, ImageWithDimensions> = mutableMapOf(),
) {
    /**
     * Calculates logo width and heigh amounts so that their ratio matches
     * the image ratio and their total area is LOGO_AREA.
     */
    fun loadImage(key: String): ImageWithDimensions = imageCache.getOrPut(
        key = key,
        defaultValue = {
            val img = bundle.imageForResource(key)!!

            val (w, h) = img.size.useContents { width to height }
            val area = (Preferences.LOGO_SIZE.toDouble() * specs.pxScale).pow(2)
            val logoHeight = sqrt(area / (w / h))
            val logoWidth = area / logoHeight

            ImageWithDimensions(nsImage = img, logoWidth = logoWidth, logoHeight = logoHeight)
        }
    )
}
