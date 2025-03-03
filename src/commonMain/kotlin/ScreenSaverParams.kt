import imagesets.ImageSet

data class ScreenSaverParams(
    val logoSize: Int,
    val logoCount: Int,
    val speed: Int,
    val useCompose: Boolean,
    val debugMode: Boolean,
    val demoMode: Boolean,
    val imageSet: ImageSet,
)
