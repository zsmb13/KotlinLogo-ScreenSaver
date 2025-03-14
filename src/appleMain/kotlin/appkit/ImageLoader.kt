package appkit

import ScreenSpecs
import config.GlobalPreferences
import config.UserDefaultsPreferences.APP_ID
import imagesets.AssetImageSet
import imagesets.CustomFolderImageSet
import imagesets.ImageSet
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.NSImage
import platform.AppKit.imageForResource
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import util.debugLog
import kotlin.math.pow
import kotlin.math.sqrt

val bundle by lazy {
    NSBundle.bundleWithIdentifier(APP_ID)!!
}

data class ImageWithDimensions(
    val nsImage: NSImage,
    val logoWidth: Double,
    val logoHeight: Double,
)

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
            val image = imageSet.images[index]
            val nsImage: NSImage = when (imageSet) {
                is AssetImageSet -> bundle.imageForResource(image)!!
                is CustomFolderImageSet -> NSImage(contentsOfURL = NSURL(string = image))
            }

            debugLog { "Loaded $index from $imageSet: $nsImage"}

            val (w, h) = nsImage.size.useContents { width to height }
            val area = (GlobalPreferences.LOGO_SIZE.toDouble() * specs.pxScale).pow(2)
            val logoHeight = sqrt(area / (w / h))
            val logoWidth = area / logoHeight

            ImageWithDimensions(nsImage = nsImage, logoWidth = logoWidth, logoHeight = logoHeight)
        }
    )
}
