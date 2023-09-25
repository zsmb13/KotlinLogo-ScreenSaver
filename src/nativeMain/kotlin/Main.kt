@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.NSColor
import platform.AppKit.NSImage
import platform.AppKit.NSImageView
import platform.AppKit.NSRectFill
import platform.AppKit.imageForResource
import platform.Foundation.NSBundle
import platform.Foundation.NSMakeRect
import platform.Foundation.NSRect
import platform.ScreenSaver.ScreenSaverView
import kotlin.math.sqrt
import kotlin.random.Random

private const val LOGO_AREA = 300 * 300
private const val LOGO_COUNT = 1
private const val SPEED = 1.5

fun create(): KotlinScreenSaverView = KotlinLogosViewImpl()

abstract class KotlinScreenSaverView {
    protected lateinit var view: ScreenSaverView
        private set

    protected lateinit var bundle: NSBundle
        private set

    open fun init(screenSaverView: ScreenSaverView, isPreview: Boolean, bundle: NSBundle) {
        this.view = screenSaverView
        this.bundle = bundle
    }

    abstract fun draw(rect: CPointer<NSRect>)
    abstract fun animateOneFrame()
}

class KotlinLogosViewImpl : KotlinScreenSaverView() {
    private val logos: List<BouncingLogo> by lazy {
        List(LOGO_COUNT) { BouncingLogo(view, bundle) }
    }

    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean, bundle: NSBundle) {
        super.init(screenSaverView, isPreview, bundle)
        screenSaverView.animationTimeInterval = 1 / 120.0
    }

    override fun draw(rect: CPointer<NSRect>) {
        clearStage()
        logos.forEach(BouncingLogo::draw)
    }

    override fun animateOneFrame() {
        logos.forEach(BouncingLogo::animateOneFrame)
        view.setNeedsDisplayInRect(view.frame)
    }

    private fun clearStage() {
        NSColor.blackColor.setFill()
        NSRectFill(view.frame)
    }
}

val images = listOf(
    "kotlin0_10x",
    "kotlin1_10x",
    "kotlin2_10x",
    "kotlin3_10x",
    "kotlin4_10x",
)

enum class Side {
    Left,
    Right,
    Top,
    Bottom,
}

class BouncingLogo(
    private val view: ScreenSaverView,
    private val bundle: NSBundle,
) {
    private var xDelta = if (Random.nextBoolean()) 1.0 else -1.0
    private var yDelta = if (Random.nextBoolean()) 1.0 else -1.0

    private var logoWidth = 300.0
    private var logoHeight = 300.0

    private val screenWidth = view.frame.useContents { this.size.width }
    private val screenHeight = view.frame.useContents { this.size.height }

    private var xPos: Double
    private var yPos: Double

    init {
        val margin = sqrt(LOGO_AREA.toDouble())
        xPos = Random.nextDouble(margin, screenWidth - margin)
        yPos = Random.nextDouble(margin, screenHeight - margin)
    }

    private val right: Double get() = xPos + logoWidth / 2
    private val left: Double get() = xPos - logoWidth / 2
    private val top: Double get() = yPos + logoHeight / 2
    private val bottom: Double get() = yPos - logoHeight / 2

    private var index = Random.nextInt(images.size)

    private val imageView = NSImageView().apply {
        image = loadImage(index)
        frame = NSMakeRect(x = xPos - logoWidth / 2, y = yPos - logoHeight / 2, w = logoWidth, h = logoHeight)
        view.addSubview(this)
    }

    fun draw() {
        imageView.frame = NSMakeRect(x = xPos - logoWidth / 2, y = yPos - logoHeight / 2, w = logoWidth, h = logoHeight)
    }

    fun animateOneFrame() {
        xPos += xDelta * SPEED
        yPos += yDelta * SPEED

        when {
            xDelta > 0 && right >= screenWidth -> {
                xDelta = -1.0
                bounce(Side.Right)
            }

            yDelta > 0 && top >= screenHeight -> {
                yDelta = -1.0
                bounce(Side.Top)
            }

            xDelta < 0 && left <= 0 -> {
                xDelta = 1.0
                bounce(Side.Left)
            }

            yDelta < 0 && bottom <= 0 -> {
                yDelta = 1.0
                bounce(Side.Bottom)
            }
        }
    }

    private fun bounce(side: Side) {
        index = (index + 1) % images.size
        imageView.image = loadImage(index)

        when (side) {
            Side.Left -> xPos = logoWidth / 2
            Side.Right -> xPos = screenWidth - logoWidth / 2
            Side.Top -> yPos = screenHeight - logoHeight / 2
            Side.Bottom -> yPos = logoHeight / 2
        }
    }

    /**
     * Sets logo width and heigh amounts so that their ratio matches
     * the image ratio and their total area is LOGO_AREA.
     */
    private fun loadImage(index: Int): NSImage {
        return bundle.imageForResource(images[index])!!.also { img ->
            val (w, h) = img.size.useContents { width to height }
            logoHeight = sqrt(LOGO_AREA / (w / h))
            logoWidth = LOGO_AREA / logoHeight
        }
    }
}