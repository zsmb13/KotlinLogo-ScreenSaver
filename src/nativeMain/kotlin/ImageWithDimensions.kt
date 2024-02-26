import platform.AppKit.NSImage

data class ImageWithDimensions(
    val nsImage: NSImage,
    val logoWidth: Double,
    val logoHeight: Double,
)
