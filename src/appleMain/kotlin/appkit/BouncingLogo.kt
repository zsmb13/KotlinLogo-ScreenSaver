package appkit

import ScreenSpecs
import config.GlobalPreferences
import imagesets.ImageSet
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSImage
import platform.AppKit.NSImageScaleProportionallyUpOrDown
import platform.AppKit.NSImageView
import platform.Foundation.NSMakeRect
import platform.ScreenSaver.ScreenSaverView
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
class BouncingLogo(
    private val view: ScreenSaverView,
    private val imageSet: ImageSet,
    private val specs: ScreenSpecs,
    private val imageLoader: ImageLoader,
) {
    private enum class Side { Left, Right, Top, Bottom, }

    // Initialized later based on image size
    private var logoWidth = 0.0
    private var logoHeight = 0.0

    private var xPos: Double
    private var yPos: Double

    private val speed = specs.pxScale * GlobalPreferences.SPEED / 10.0 * Random.nextDouble(0.9, 1.1)

    private var xDelta = speed * if (Random.nextBoolean()) 1.0 else -1.0
    private var yDelta = speed * if (Random.nextBoolean()) 1.0 else -1.0

    init {
        val margin = GlobalPreferences.LOGO_SIZE * specs.pxScale
        xPos = Random.nextDouble(margin, specs.screenWidth - margin)
        yPos = Random.nextDouble(margin, specs.screenHeight - margin)
    }

    private val right: Double get() = xPos + logoWidth / 2
    private val left: Double get() = xPos - logoWidth / 2
    private val top: Double get() = yPos + logoHeight / 2
    private val bottom: Double get() = yPos - logoHeight / 2

    private var index = Random.nextInt(imageSet.size)

    private val imageView = NSImageView().apply {
        imageScaling = NSImageScaleProportionallyUpOrDown
        image = updateImage()
        frame = NSMakeRect(x = xPos - logoWidth / 2, y = yPos - logoHeight / 2, w = logoWidth, h = logoHeight)
        view.addSubview(this)
    }

    fun draw() {
        imageView.frame = NSMakeRect(x = xPos - logoWidth / 2, y = yPos - logoHeight / 2, w = logoWidth, h = logoHeight)
    }

    fun animateFrame() {
        xPos += xDelta
        yPos += yDelta

        when {
            xDelta > 0 && right >= specs.screenWidth -> bounce(Side.Right)
            yDelta > 0 && top >= specs.screenHeight -> bounce(Side.Top)
            xDelta < 0 && left <= 0 -> bounce(Side.Left)
            yDelta < 0 && bottom <= 0 -> bounce(Side.Bottom)
        }
    }

    private fun bounce(side: Side) {
        index = (index + 1) % imageSet.size
        imageView.image = updateImage()

        when (side) {
            Side.Left -> {
                xPos = logoWidth / 2
                xDelta *= -1
            }

            Side.Right -> {
                xPos = specs.screenWidth - logoWidth / 2
                xDelta *= -1
            }

            Side.Top -> {
                yPos = specs.screenHeight - logoHeight / 2
                yDelta *= -1
            }

            Side.Bottom -> {
                yPos = logoHeight / 2
                yDelta *= -1
            }
        }
    }

    private fun updateImage(): NSImage {
        val (image, w, h) = imageLoader.loadImage(imageSet, index)
        logoWidth = w
        logoHeight = h
        return image
    }

    fun dispose() {
        imageView.removeFromSuperview()
    }
}
