package imagesets

class OrderedImageSet(override val name: String, assetNames: List<String>) : AssetImageSet {
    override val images: List<String> = assetNames
    override val size: Int = images.size
}
