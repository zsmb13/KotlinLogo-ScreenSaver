//import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import config.KotlinLogosPrefController
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
import platform.AppKit.NSView
import platform.AppKit.NSWindow
import platform.Foundation.NSMakeRect
import platform.ScreenSaver.ScreenSaverView
import util.debugLog
import util.ispreview
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

private const val framesPerSecond = 60

class ComposeScreenSaverView : KotlinScreenSaverView() {
    private val preferencesController by lazy { KotlinLogosPrefController() }
    override val configureSheet: NSWindow?
        get() = preferencesController.window

    val list = mutableListOf<BouncingLogo2>()

    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        debugLog { "ComposeScreenSaverView initing, isPreview: $isPreview" }
        super.init(screenSaverView, isPreview)
        screenSaverView.animationTimeInterval = 1.0 / framesPerSecond
        ispreview = isPreview
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

    lateinit var composeView: NSView

    val imageSet = imageSets[Preferences.LOGO_SET]
    val imageUrl = imageSet.images[0]


    val path = Path(imageUrl.substringAfter("file://"))
    val systemFileSystem = SystemFileSystem
    val source = systemFileSystem.source(path)
    val bufferedSource = source.buffered()

    val bytes = bufferedSource.use { it.readByteArray() }

    // real values
    val specs = ScreenSpecs(screenSaverView)
    val imageLoader = ImageLoader(specs)

//    logos += List(Preferences.LOGO_COUNT) {
    logos += List(1) {
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

        val svgPainter = bytes.decodeToSvgPainter(LocalDensity.current)
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
            contentAlignment = Alignment.TopStart,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color.Black)
            }

            logos.forEach {
                it.Content()
            }

            Box(
                Modifier
                    .graphicsLayer {
                        rotationY = animatedRotation
                        rotationX = animatedRotation
                    }
                    .size(100.dp)
                    .background(color)
                    .align(Alignment.Center)
            )

            Image(
                svgPainter, null, modifier = Modifier.size(100.dp).align(Alignment.Center)
            )
        }
    }

    composeView.frame = NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight)
    screenSaverView.addSubview(composeView)
}

class BouncingLogo2(
    private val imageSet: ImageSet,
    private val specs: ScreenSpecs,
    private val imageLoader: ImageLoader,
) {
    private enum class Side { Left, Right, Bottom, Top, }

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
    val area = (Preferences.LOGO_SIZE.toDouble() * specs.pxScale).pow(2)

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

    private val right: Double get() = (xPos + logoWidth / 2)
    private val left: Double get() = (xPos - logoWidth / 2)
    private val bottom: Double get() = (yPos + logoHeight / 2)
    private val top: Double get() = (yPos - logoHeight / 2)

    fun animateFrame() {
        xPos += xDelta
        yPos += yDelta

//        debugLog { "pos: $xPos,$yPos - delta: $xDelta,$yDelta - lr: $left,$right - tb: $top,$bottom - screen: ${specs.screenWidth}, ${specs.screenHeight}" }

        when {
            xDelta > 0 && right >= specs.screenWidth -> bounce(Side.Right)
            yDelta > 0 && bottom >= specs.screenHeight -> bounce(Side.Bottom)
            xDelta < 0 && left <= 0 -> bounce(Side.Left)
            yDelta < 0 && top <= 0 -> bounce(Side.Top)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun Content() {
        val painter = remember(bytes) {
            bytes.decodeToSvgPainter(Density(1f)).also { p ->
                logoHeight = sqrt(area / (p.intrinsicSize.width / p.intrinsicSize.height))
                logoWidth = area / logoHeight
            }
        }

        val density = LocalDensity.current.density
        val targetX by animateFloatAsState(((xPos - logoWidth / 2) * density).toFloat())
        val targetY by animateFloatAsState(((yPos - logoHeight / 2) * density).toFloat())

        Column(
            Modifier
                .graphicsLayer {
                    this.translationX = targetX
                    this.translationY = targetY
                }
                .size(width = logoWidth.dp, height = logoHeight.dp)
        ) {
            Image(painter, null, modifier = Modifier.fillMaxSize())
        }
    }

    private fun bounce(side: Side) {
        index = (index + 1) % imageSet.size
//        imageView.image = updateImage()

        debugLog { "Bouncing on $side" }
        when (side) {
            Side.Left -> {
                xPos = logoWidth / 2
                xDelta *= -1
            }

            Side.Right -> {
                xPos = specs.screenWidth - logoWidth / 2
                xDelta *= -1
            }

            Side.Bottom -> {
                yPos = specs.screenHeight - logoHeight / 2
                yDelta *= -1
            }

            Side.Top -> {
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
