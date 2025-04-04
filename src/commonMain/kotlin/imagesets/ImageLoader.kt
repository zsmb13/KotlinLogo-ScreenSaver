package imagesets

data class LoadedImage<I>(
    val image: I,
    val width: Float,
    val height: Float,
)

interface ImageLoader<I> {
    fun loadImage(
        imageSet: ImageSet,
        index: Int,
        adjustSize: Adjuster,
    ): LoadedImage<I>?
}

/**
 * Convert image aspect ratio to target width and height values.
 */
typealias Adjuster = (Float) -> Pair<Float, Float>
