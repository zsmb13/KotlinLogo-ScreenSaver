package imagesets

class AndroidResImageSet(
    override val name: String,
    override val images: List<String>,
) : ImageSet {
    override val size: Int = images.size
}
