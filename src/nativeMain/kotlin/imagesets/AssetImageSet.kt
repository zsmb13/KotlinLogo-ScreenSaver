package imagesets

import config.Preferences.APP_ID
import platform.AppKit.NSImage
import platform.AppKit.imageForResource
import platform.Foundation.NSBundle

val bundle by lazy {
    NSBundle.bundleWithIdentifier(APP_ID)!!
}

interface AssetImageSet : ImageSet {
    override fun load(image: String): NSImage = bundle.imageForResource(image)!!
}
