package imagesets

import config.GlobalPreferences
import util.debugLog

sealed interface ImageSet {
    val name: String
    val images: List<String>
    val size: Int
}

interface AssetImageSet : ImageSet

class OrderedAssetImageSet(override val name: String, assetNames: List<String>) : AssetImageSet {
    override val images: List<String> = assetNames
    override val size: Int = images.size
}

class RandomizedAssetImageSet(override val name: String, private var assetNames: List<String>) : AssetImageSet {
    override val images: List<String>
        get() = assetNames.shuffled()
    override val size: Int = images.size
}

val imageSets: MutableList<ImageSet> = mutableListOf<ImageSet>(
    OrderedAssetImageSet(
        "Kotlin logos",
        listOf(
            "kotlin0",
            "kotlin1",
            "kotlin2",
            "kotlin3",
            "kotlin4",
        )
    ),
    RandomizedAssetImageSet(
        "Kodee",
        listOf(
            "kodee-greeting",
            "kodee-inlove",
            "kodee-jumping",
            "kodee-naughty",
            "kodee-sharing",
            "kodee-sitting",
            "kodee-waving",
        )
    ),
).apply {
    val customFolder = GlobalPreferences.CUSTOM_FOLDER
    if (customFolder.isEmpty()) {
        // No custom folder set
        return@apply
    }

    val custom = CustomFolderImageSet(customFolder)
    if (custom != null) {
        add(custom)
    } else {
        debugLog { "Failed to load custom folder '$custom', resetting to default image set" }
        GlobalPreferences.CUSTOM_FOLDER = ""
        GlobalPreferences.LOGO_SET = 0
    }
}
