import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToSvgPainter
import platform.AppKit.NSView
import platform.AppKit.NSWindow
import platform.Foundation.NSMakeRect
import platform.ScreenSaver.ScreenSaverView
import util.debugLog
import util.ispreview
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

private const val framesPerSecond = 60

class ComposeScreenSaverView : KotlinScreenSaverView() {
    private val preferencesController by lazy { KotlinLogosPrefController() }
    override val configureSheet: NSWindow?
        get() = preferencesController.window

    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        debugLog { "ComposeScreenSaverView initing, isPreview: $isPreview" }
        super.init(screenSaverView, isPreview)
        screenSaverView.animationTimeInterval = 1.0 / framesPerSecond
        ispreview = isPreview
        attach(screenSaverView)
        debugLog { "ComposeScreenSaverView inited, isPreview: $isPreview" }
    }

    override fun animateOneFrame() = Unit
}

@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
fun attach(
    screenSaverView: ScreenSaverView,
) {
    lateinit var composeView: NSView

    val imageSet = imageSets[Preferences.LOGO_SET]
    val specs = ScreenSpecs(screenSaverView)

    Window(
        size = DpSize(specs.screenWidth.dp, specs.screenHeight.dp),
    ) {
        composeView = this.window.contentView!!

        val density = LocalDensity.current
        val imgLoader = remember(density) {
            ImageLoader2(density, specs)
        }

        val screenW = remember { specs.screenWidth.toFloat() * density.density }
        val screenH = remember { specs.screenHeight.toFloat() * density.density }

        Box(
            Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.TopStart,
        ) {
            repeat(10) {
                BouncingLogo2(
                    imageSet = imageSet,
                    imgLoader = imgLoader,
                    screenW = screenW,
                    screenH = screenH,
                    pxScale = specs.pxScale,
                )
            }
        }
    }

    composeView.frame = NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight)
    screenSaverView.addSubview(composeView)
}

data class LogoWithSizes(
    val painter: Painter,
    val width: Float,
    val height: Float,
)

class ImageLoader2(private val density: Density, private val specs: ScreenSpecs) {
    private val imageCache: MutableMap<String, LogoWithSizes> = mutableMapOf()

    private val area = (Preferences.LOGO_SIZE.toFloat() * specs.pxScale).pow(2).toFloat()

    @OptIn(ExperimentalResourceApi::class)
    fun loadImage(imageSet: ImageSet, index: Int): LogoWithSizes {
        val imagePath = imageSet.images[index]
        return imageCache.getOrPut(
            key = imagePath,
            defaultValue = {
                val painter = SystemFileSystem.source(Path(imagePath.substringAfter("file://")))
                    .buffered()
                    .use(Source::readByteArray)
                    .decodeToSvgPainter(density)
                val logoHeight = sqrt(area / (painter.intrinsicSize.width / painter.intrinsicSize.height)).toFloat()
                val logoWidth = area / logoHeight
                LogoWithSizes(
                    painter = painter,
                    width = logoWidth * density.density,
                    height = logoHeight * density.density,
                )
            }
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BouncingLogo2(
    imageSet: ImageSet,
    imgLoader: ImageLoader2,
    screenW: Float,
    screenH: Float,
    pxScale: Double,
) {
    val density = LocalDensity.current.density
    var index by remember { mutableIntStateOf(Random.nextInt(imageSet.size)) }

    val (painter, logoWidth, logoHeight) = remember(imageSet, index) {
        imgLoader.loadImage(imageSet, index)
    }

    val speed = remember { (pxScale * Preferences.SPEED / 10.0 * Random.nextDouble(0.9, 1.1)).toFloat() }

    var xDelta by remember { mutableStateOf(speed * if (Random.nextBoolean()) 1 else -1) }
    var yDelta by remember { mutableStateOf(speed * if (Random.nextBoolean()) 1 else -1) }

    val margin = remember { Preferences.LOGO_SIZE * pxScale }

    val animX = remember { Animatable(Random.nextDouble(margin, screenW - margin).toFloat()) }
    val animY = remember { Animatable(Random.nextDouble(margin, screenH - margin).toFloat()) }

    LaunchedEffect(Unit) {
        while (true) {
            val currentX = animX.value
            val currentY = animY.value

            val remainingX =
                if (xDelta > 0) screenW - (currentX + logoWidth / 2)
                else (currentX - logoWidth / 2)
            val remainingY =
                if (yDelta > 0) screenH - (currentY + logoHeight / 2)
                else (currentY - logoHeight / 2)

            val xSmaller = remainingX < remainingY
            val remaining = if (xSmaller) remainingX else remainingY
            val duration = ((remaining / abs(yDelta)) * 60 / density / density).toInt()
            val xTarget = currentX + remaining * if (xDelta > 0) 1 else -1
            val yTarget = currentY + remaining * if (yDelta > 0) 1 else -1

            awaitAll(
                async {
                    animX.animateTo(
                        xTarget, tween(
                            durationMillis = duration,
                            easing = LinearEasing,
                        )
                    )
                },
                async {
                    animY.animateTo(
                        yTarget, tween(
                            durationMillis = duration,
                            easing = LinearEasing,
                        )
                    )
                }
            )

            if (xSmaller) {
                xDelta *= -1
            } else {
                yDelta *= -1
            }
            index = (index + 1) % imageSet.size
        }
    }

    Column(
        Modifier
            .graphicsLayer {
                translationX = animX.value - logoWidth.dp.toPx() / 2
                translationY = animY.value - logoHeight.dp.toPx() / 2
            }
            .size(width = logoWidth.dp, height = logoHeight.dp)
            .background(Color.White)
    ) {
        Image(painter, null, modifier = Modifier.fillMaxSize())
    }
}
