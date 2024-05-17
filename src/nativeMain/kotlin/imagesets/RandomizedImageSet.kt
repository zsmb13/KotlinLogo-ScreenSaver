package imagesets

class RandomizedImageSet(override val name: String, private var assetNames: List<String>) : AssetImageSet {
    override val images: List<String>
        get() = assetNames.shuffled()
    override val size: Int = images.size
}
