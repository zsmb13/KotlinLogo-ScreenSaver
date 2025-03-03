package appkit

import config.APP_ID
import imagesets.Adjuster
import imagesets.AssetImageSet
import imagesets.CustomFolderImageSet
import imagesets.ImageLoader
import imagesets.ImageSet
import imagesets.LoadedImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.NSDataAsset
import platform.AppKit.NSImage
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

val bundle by lazy {
    requireNotNull(NSBundle.bundleWithIdentifier(APP_ID))
}

@OptIn(ExperimentalForeignApi::class)
class NSImageLoader : ImageLoader<NSImage> {

    /**
     * Calculates logo width and heigh amounts so that their ratio matches
     * the image ratio and their total area is LOGO_AREA.
     */
    override fun loadImage(imageSet: ImageSet, index: Int, adjuster: Adjuster): LoadedImage<NSImage> {
        val image = imageSet.images[index]
        val nsImage: NSImage = when (imageSet) {
            is AssetImageSet -> NSImage(data = NSDataAsset(name = image, bundle).data)
            is CustomFolderImageSet -> NSImage(contentsOfURL = NSURL(string = image))
        }
        val (imgWidth, imgHeight) = nsImage.size.useContents { width.toFloat() to height.toFloat() }
        val (width, height) = adjuster(imgWidth / imgHeight)
        return LoadedImage(image = nsImage, width = width, height = height)
    }
}
