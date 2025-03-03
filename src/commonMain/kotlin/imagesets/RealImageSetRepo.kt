package imagesets

import config.PreferenceStorage
import util.debugLog

interface ImageSetRepo {
    fun getCurrentImageSet(): ImageSet
    fun getImageSets(): List<ImageSet>
}

class RealImageSetRepo(
    private var preferenceStorage: PreferenceStorage
) : ImageSetRepo {
    override fun getCurrentImageSet(): ImageSet = getImageSets()[preferenceStorage.getPreferences().logoSet]

    private var loadedCustomFolder = preferenceStorage.getPreferences().customFolder
    private var imageSets = createImageSets(loadedCustomFolder)

    override fun getImageSets(): List<ImageSet> {
        val currentCustomFolder = preferenceStorage.getPreferences().customFolder
        if (loadedCustomFolder != currentCustomFolder) {
            imageSets = createImageSets(currentCustomFolder)
            loadedCustomFolder = currentCustomFolder
        }
        return imageSets
    }

    private fun createImageSets(currentCustomFolder: String): List<ImageSet> {
        return buildList {
            add(
                OrderedAssetImageSet(
                    "Kotlin logos",
                    listOf(
                        "kotlin0",
                        "kotlin1",
                        "kotlin2",
                        "kotlin3",
                        "kotlin4"
                    )
                )
            )
            add(
                RandomizedAssetImageSet(
                    "Kodee",
                    listOf(
                        "kodee-greeting",
                        "kodee-inlove",
                        "kodee-jumping",
                        "kodee-naughty",
                        "kodee-sharing",
                        "kodee-sitting",
                        "kodee-waving"
                    )
                )
            )

            if (currentCustomFolder.isNotEmpty()) {
                val custom = CustomFolderImageSet(currentCustomFolder)
                if (custom != null) {
                    debugLog { "Successfully loaded custom folder '$custom'" }
                    add(custom)
                } else {
                    debugLog { "Failed to load custom folder '$custom', resetting to default image set" }
                    preferenceStorage.update {
                        customFolder = ""
                        logoSet = 0
                    }
                }
            }
        }
    }
}
