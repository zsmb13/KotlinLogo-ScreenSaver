package imagesets

sealed interface ImageSet {
    val name: String
    val images: List<String>
    val size: Int
}

interface AssetImageSet : ImageSet

class OrderedAssetImageSet(override val name: String, assetNames: List<String>) : AssetImageSet {
    override val images: List<String> = assetNames
    override val size: Int = images.size

    override fun equals(other: Any?): Boolean {
        if (other !is OrderedAssetImageSet) return false
        return name == other.name && images == other.images
    }
}

class RandomizedAssetImageSet(override val name: String, private var assetNames: List<String>) : AssetImageSet {
    override val images: List<String> = assetNames.shuffled()
    override val size: Int = images.size

    override fun equals(other: Any?): Boolean {
        if (other !is RandomizedAssetImageSet) return false
        return name == other.name && images == other.images
    }
}
