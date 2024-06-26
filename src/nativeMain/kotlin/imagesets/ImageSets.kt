package imagesets

import config.Preferences
import platform.AppKit.NSImage
import util.debugLog

sealed interface ImageSet {
    val name: String
    val images: List<String>
    val size: Int
    fun load(image: String): NSImage
}

val imageSets: MutableList<ImageSet> = mutableListOf<ImageSet>(
    OrderedImageSet(
        "Kotlin logos",
        listOf(
            "kotlin0",
            "kotlin1",
            "kotlin2",
            "kotlin3",
            "kotlin4",
        )
    ),
    RandomizedImageSet(
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
    val customFolder = Preferences.CUSTOM_FOLDER
    if (customFolder.isEmpty()) {
        // No custom folder set
        return@apply
    }

    val custom = CustomFolderImageSet.load(customFolder)
    if (custom != null) {
        add(custom)
    } else {
        debugLog { "Failed to load custom folder '$custom', resetting to default image set" }
        Preferences.CUSTOM_FOLDER = ""
        Preferences.LOGO_SET = 0
    }
}
