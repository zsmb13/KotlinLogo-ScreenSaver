package imagesets

class CustomFolderImageSet internal constructor(
    override val name: String,
    override val images: List<String>,
) : ImageSet {
    override val size: Int = images.size
}

expect fun CustomFolderImageSet(folderUrl: String): CustomFolderImageSet?
