//import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import config.Preferences
import imagesets.ImageSet
import imagesets.imageSets
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToSvgPainter
import platform.AppKit.NSImage
import platform.AppKit.NSImageScaleProportionallyUpOrDown
import platform.AppKit.NSImageView
import platform.AppKit.NSView
import platform.Foundation.NSMakeRect
import platform.ScreenSaver.ScreenSaverView
import util.debugLog
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class ComposeScreenSaverView : KotlinScreenSaverView() {
    val list = mutableListOf<BouncingLogo2>()
    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        debugLog { "ComposeScreenSaverView initing, isPreview: $isPreview" }
        super.init(screenSaverView, isPreview)
        attach(screenSaverView, list)
        debugLog { "ComposeScreenSaverView inited, isPreview: $isPreview" }
    }

    override fun animateOneFrame() {
        list.forEach {
            it.animateFrame()
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
fun attach(screenSaverView: ScreenSaverView, logos: MutableList<BouncingLogo2>) {
    debugLog { "attempting to attach" }

//    NSApplication.sharedApplication()
    lateinit var composeView: NSView

    val imageSet = imageSets[Preferences.LOGO_SET]
    val imageUrl = imageSet.images[0]
    debugLog { "imageUrl is $imageUrl" }


//    val x = "file:///Users/zsmb/screensaver-images/10_amper.svg"

    val path = Path(imageUrl.substringAfter("file://"))
    debugLog { "path is $path" }
    val systemFileSystem = SystemFileSystem
    debugLog { "systemFileSystem is $systemFileSystem" }
    val source = systemFileSystem.source(path)
    debugLog { "source is $source" }
    val bufferedSource = source.buffered()
    debugLog { "bufferedSource is $bufferedSource" }

    val bytes = bufferedSource.use { it.readByteArray() }
    debugLog { "bytes is ${bytes.size}, $bytes" }


//    val bitmap = bytes.decodeToImageBitmap()
//    debugLog { "bitmap is $bitmap" }

    val nsImg = imageSet.load(imageUrl)


    // real values
    val specs = ScreenSpecs(screenSaverView)
    val imageLoader = ImageLoader(specs)
    val set = imageSets[Preferences.LOGO_SET]

    logos += List(10) {
        BouncingLogo2(
            imageSet = imageSet,
            specs = specs,
            imageLoader = imageLoader,
        )
    }

    Window(
        size = DpSize(specs.screenWidth.dp, specs.screenHeight.dp),
    ) {
        composeView = this.window.contentView!!

        debugLog { "gonna decode $bytes" }
        val svgPainter = bytes.decodeToSvgPainter(LocalDensity.current)
        debugLog { "decoded $bytes into $svgPainter" }

        var on by remember { mutableStateOf(false) }
        val color by animateColorAsState(if (!on) Color.Red else Color.Green)

        val infiniteTransition = rememberInfiniteTransition()
        val animatedRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Box(
            Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color.Black)
            }

            logos.forEach {
                it.draw()
            }

            Box(
                Modifier
                    .graphicsLayer {
                        rotationY = animatedRotation
                        rotationX = animatedRotation
                    }
                    .size(100.dp)
                    .background(color)
            )

            Image(
                svgPainter, null, modifier = Modifier.size(100.dp)
            )
        }
    }

    composeView.frame = NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight)
    screenSaverView.addSubview(composeView)

    val nsImageView = NSImageView ().apply {
        imageScaling = NSImageScaleProportionallyUpOrDown
        frame = NSMakeRect(x = 0.0, y = 0.0, w = 10.0, h = 10.0)
        image = nsImg
    }
    screenSaverView.addSubview(nsImageView)
//    NSApp?.run()
}

class BouncingLogo2(
    private val imageSet: ImageSet,
    private val specs: ScreenSpecs,
    private val imageLoader: ImageLoader,
) {
    private enum class Side { Left, Right, Top, Bottom, }

    // Initialized later based on image size
    private var logoWidth = 0.0
    private var logoHeight = 0.0

    private var index = Random.nextInt(imageSet.size)

    val imageUrl = imageSet.images[index]
    val path = Path(imageUrl.substringAfter("file://"))
    val systemFileSystem = SystemFileSystem
    val source = systemFileSystem.source(path)
    val bufferedSource = source.buffered()
    val bytes = bufferedSource.use { it.readByteArray() }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun draw() {
        val painter = bytes.decodeToSvgPainter(LocalDensity.current)


        val area = (Preferences.LOGO_SIZE.toDouble() * specs.pxScale).pow(2)
        val logoHeight = sqrt(area / (painter.intrinsicSize.width / painter.intrinsicSize.height))
        val logoWidth = area / logoHeight

        Image(painter, null, modifier = Modifier.graphicsLayer {
            this.translationX = xPos.toFloat()
            this.translationY = yPos.toFloat()
        }.size(width = logoWidth.dp, height = logoHeight.dp))
    }

    private var xPos by mutableStateOf<Double>(0.0)
    private var yPos by mutableStateOf<Double>(0.0)

    private val speed = specs.pxScale * Preferences.SPEED / 10.0 * Random.nextDouble(0.9, 1.1)

    private var xDelta = speed * if (Random.nextBoolean()) 1.0 else -1.0
    private var yDelta = speed * if (Random.nextBoolean()) 1.0 else -1.0

    init {
        val margin = Preferences.LOGO_SIZE * specs.pxScale
        xPos = Random.nextDouble(margin, specs.screenWidth - margin)
        yPos = Random.nextDouble(margin, specs.screenHeight - margin)
    }

    private val right: Double get() = xPos + logoWidth / 2
    private val left: Double get() = xPos - logoWidth / 2
    private val top: Double get() = yPos + logoHeight / 2
    private val bottom: Double get() = yPos - logoHeight / 2

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
//        imageView.image = updateImage()

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
}
