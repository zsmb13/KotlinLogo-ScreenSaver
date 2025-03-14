import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    val list = mutableListOf<BouncingLogo2>()

    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        debugLog { "ComposeScreenSaverView initing, isPreview: $isPreview" }
        super.init(screenSaverView, isPreview)
        screenSaverView.animationTimeInterval = 1.0 / framesPerSecond
        ispreview = isPreview
        attach(screenSaverView, list)
        debugLog { "ComposeScreenSaverView inited, isPreview: $isPreview" }
    }

    override fun animateOneFrame() = Unit
}

@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
fun attach(
    screenSaverView: ScreenSaverView,
    logos: MutableList<BouncingLogo2>,
) {

    lateinit var composeView: NSView

    val imageSet = imageSets[Preferences.LOGO_SET]
    val specs = ScreenSpecs(screenSaverView)

    logos += List(10) {
        BouncingLogo2(
            imageSet = imageSet,
            specs = specs,
        )
    }

    Window(
        size = DpSize(specs.screenWidth.dp, specs.screenHeight.dp),
    ) {
        composeView = this.window.contentView!!

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
        }
    }

    composeView.frame = NSMakeRect(0.0, 0.0, specs.screenWidth, specs.screenHeight)
    screenSaverView.addSubview(composeView)
}

class BouncingLogo2(
    private val imageSet: ImageSet,
    private val specs: ScreenSpecs,
) {
    private enum class Side { Left, Right, Bottom, Top, }

    // Initialized later based on image size
    private var logoWidth = 0f
    private var logoHeight = 0f

    private var index = Random.nextInt(imageSet.size)

    val imageUrl = imageSet.images[index]
    val bytes = SystemFileSystem.source(Path(imageUrl.substringAfter("file://")))
        .buffered()
        .use { it.readByteArray() }

    val area = (Preferences.LOGO_SIZE.toFloat() * specs.pxScale).pow(2).toFloat()


    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun Content() {
        val density = LocalDensity.current.density

        val painter = remember(bytes) {
            bytes.decodeToSvgPainter(Density(1f)).also { p ->
                logoHeight = sqrt(area / (p.intrinsicSize.width / p.intrinsicSize.height)).toFloat() * density
                logoWidth = area / logoHeight * density
            }
        }

        val screenW = specs.screenWidth.toFloat() * density
        val screenH = specs.screenHeight.toFloat() * density

        val speed = remember { (specs.pxScale * Preferences.SPEED / 10.0 * Random.nextDouble(0.9, 1.1)).toFloat() }

        var xDelta by remember { mutableStateOf(speed * if (Random.nextBoolean()) 1 else -1) }
        var yDelta by remember { mutableStateOf(speed * if (Random.nextBoolean()) 1 else -1) }

        val margin = Preferences.LOGO_SIZE * specs.pxScale

        val animX = remember { Animatable(Random.nextDouble(margin, screenW - margin).toFloat()) }
        val animY = remember { Animatable(Random.nextDouble(margin, screenH - margin).toFloat()) }

        LaunchedEffect(Unit) {
            debugLog { "INITIAL: ${animX.value},${animY.value} (randomized between ${margin} and ~${specs.screenHeight - margin})" }
        }

        LaunchedEffect(xDelta, yDelta) {
            while (true) {
                // right, left
                val remainingX =
                    if (xDelta > 0) screenW.toFloat() - (animX.value + logoWidth / 2) else (animX.value - logoWidth / 2)
                // bottom, top
                val remainingY =
                    if (yDelta > 0) screenH.toFloat() - (animY.value + logoHeight / 2) else (animY.value - logoHeight / 2)

                debugLog { "remaining $remainingX,$remainingY" }


                var duration: Int

                var xTarget: Float
                var xMult: Int

                var yTarget: Float
                var yMult: Int

                if (remainingX < remainingY) {
//                if (true) {
                    duration = (remainingX / abs(xDelta) * 60 / density / density).toInt()
                    xTarget = if (xDelta > 0) {
                        animX.value + remainingX
                    } else {
                        animX.value - remainingX
                    }
                    yTarget = if (yDelta > 0) {
                        animY.value + remainingX
                    } else {
                        animY.value - remainingX
                    }
//                    debugLog { "remainingX $remainingX, xDelta $xDelta, animating over $duration to $xTarget" }
                    xMult = -1
                    yMult = 1
                } else {
                    duration = (remainingY / abs(yDelta) * 60 / density / density).toInt()
                    yTarget = if (yDelta > 0) {
                        animY.value + remainingY
                    } else {
                        animY.value - remainingY
                    }
                    xTarget = if (xDelta > 0) {
                        animX.value + remainingY
                    } else {
                        animX.value - remainingY
                    }
//                    debugLog { "remainingX $remainingX, xDelta $xDelta, animating over $duration to $xTarget" }
                    xMult = 1
                    yMult = -1
                }

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
                xDelta *= xMult
                yDelta *= yMult
                debugLog { "anim done, xDelta updated to $xDelta, yDelta to $yDelta" }
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

//    private fun bounce(side: Side) {
//        index = (index + 1) % imageSet.size
//
//        debugLog { "Bouncing on $side" }
//        when (side) {
//            Side.Left -> {
//                xPos = logoWidth / 2
//                xDelta *= -1
//            }
//
//            Side.Right -> {
//                xPos = specs.screenWidth - logoWidth / 2
//                xDelta *= -1
//            }
//
//            Side.Bottom -> {
//                yPos = specs.screenHeight - logoHeight / 2
//                yDelta *= -1
//            }
//
//            Side.Top -> {
//                yPos = logoHeight / 2
//                yDelta *= -1
//            }
//        }
//    }
}
