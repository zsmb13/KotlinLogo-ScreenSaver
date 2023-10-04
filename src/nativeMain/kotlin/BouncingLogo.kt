import config.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AppKit.NSImage
import platform.AppKit.NSImageScaleProportionallyUpOrDown
import platform.AppKit.NSImageView
import platform.AppKit.imageForResource
import platform.CoreGraphics.CGColorCreateSRGB
import platform.Foundation.NSBundle
import platform.Foundation.NSMakeRect
import platform.ScreenSaver.ScreenSaverView
import util.debugLog
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
class BouncingLogo(
    private val view: ScreenSaverView,
    private val bundle: NSBundle,
    private val images: List<String>,
) {
    private enum class Side {
        Left,
        Right,
        Top,
        Bottom,
    }

    private var xDelta = if (Random.nextBoolean()) 1.0 else -1.0
    private var yDelta = if (Random.nextBoolean()) 1.0 else -1.0

    // Initialized later based on image size
    private var logoWidth = 0.0
    private var logoHeight = 0.0

    private val screenWidth = view.frame.useContents { this.size.width }
    private val screenHeight = view.frame.useContents { this.size.height }

    private var xPos: Double
    private var yPos: Double

    // Magic numbers 🪄✨
    private var pxScale = ((screenWidth / 1728) + (screenHeight / 1117)) / 2

    private val speed = pxScale * Preferences.SPEED / 10.0 * Random.nextDouble(0.9, 1.1)

    init {
        val margin = Preferences.LOGO_SIZE * pxScale
        xPos = Random.nextDouble(margin, screenWidth - margin)
        yPos = Random.nextDouble(margin, screenHeight - margin)
    }

    private val right: Double get() = xPos + logoWidth / 2
    private val left: Double get() = xPos - logoWidth / 2
    private val top: Double get() = yPos + logoHeight / 2
    private val bottom: Double get() = yPos - logoHeight / 2

    private var index = Random.nextInt(images.size)

    private val imageView = NSImageView().apply {
        if (Preferences.IS_DEBUG) {
            wantsLayer = true
            layer?.setBackgroundColor(CGColorCreateSRGB(1.0, 1.0, 1.0, 1.0))
        }

        imageScaling = NSImageScaleProportionallyUpOrDown
        image = loadImage(index)
        frame = NSMakeRect(x = xPos - logoWidth / 2, y = yPos - logoHeight / 2, w = logoWidth, h = logoHeight)
        view.addSubview(this)
    }

    fun draw() {
        imageView.frame = NSMakeRect(x = xPos - logoWidth / 2, y = yPos - logoHeight / 2, w = logoWidth, h = logoHeight)
    }

    fun animateOneFrame() {
        xPos += xDelta * speed
        yPos += yDelta * speed

        when {
            xDelta > 0 && right >= screenWidth -> {
                debugLog { "bounce(Right), $right (lw $logoWidth, lh $logoHeight) (x $xPos y $yPos)" }
                xDelta = -1.0
                bounce(Side.Right)
            }

            yDelta > 0 && top >= screenHeight -> {
                debugLog { "bounce(Top), $top (lw $logoWidth, lh $logoHeight) (x $xPos y $yPos)" }
                yDelta = -1.0
                bounce(Side.Top)
            }

            xDelta < 0 && left <= 0 -> {
                debugLog { "bounce(Left), $left (lw $logoWidth, lh $logoHeight) (x $xPos y $yPos)" }
                xDelta = 1.0
                bounce(Side.Left)
            }

            yDelta < 0 && bottom <= 0 -> {
                debugLog { "bounce(Bottom), $bottom (lw $logoWidth, lh $logoHeight) (x $xPos y $yPos)" }
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
            debugLog { "Loaded image ${images[index]}, w $w h $h" }
            val area = (Preferences.LOGO_SIZE.toDouble() * pxScale).pow(2)
            logoHeight = sqrt(area / (w / h))
            logoWidth = area / logoHeight
        }
    }

    fun dispose() {
        imageView.removeFromSuperview()
    }
}
