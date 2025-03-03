package imagesets

@ConsistentCopyVisibility
data class CustomFolderImageSet internal constructor(
    override val name: String,
    override val images: List<String>,
) : ImageSet {
    override val size: Int = images.size

    override fun toString(): String {
        return "CustomFolderImageSet(name='$name', $size images)"
    }
}

fun String.isSupportedFormat() = substringAfterLast('.', "").lowercase() in setOf("png", "svg", "jpg", "jpeg")

expect fun CustomFolderImageSet(folderUrl: String): CustomFolderImageSet?
