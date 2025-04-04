package compose

import ScreenSaverParams
import ScreenSpecs
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import imagesets.UImageLoader
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToSvgPainter
import util.debugLog

@Composable
fun ScreenSaverContent(
    params: ScreenSaverParams,
    imgLoader: UImageLoader,
    specs: ScreenSpecs,
    onClick: () -> Unit = {},
) {
    val density = LocalDensity.current
    val screenW = remember(specs, density) { specs.screenWidth.toFloat() * density.density }
    val screenH = remember(specs, density) { specs.screenHeight.toFloat() * density.density }

    debugLog { "DENSITY IS $density; ${density.density}" }

    Box(
        Modifier.fillMaxSize()
            .clickable(onClick = onClick)
            .background(if (params.debugMode) Color.DarkGray else Color.Black),
        contentAlignment = Alignment.TopStart,
    ) {
        repeat(params.logoCount) {
            BouncingLogo(
                imageSet = params.imageSet,
                imgLoader = imgLoader,
                screenW = screenW,
                screenH = screenH,
                pxScale = specs.pxScale,
                baseSpeed = params.speed,
                logoSize = params.logoSize,
                debug = params.debugMode,
            )
        }
        if (params.demoMode) {
            DemoContent(specs)
        }
    }
}

@Composable
private fun BoxScope.DemoContent(specs: ScreenSpecs) {
    Row(
        Modifier.align(Alignment.BottomStart)
            .graphicsLayer {
                scaleX = specs.pxScale.toFloat()
                scaleY = specs.pxScale.toFloat()
                transformOrigin = TransformOrigin(0f, 1f)
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val density = LocalDensity.current

        @OptIn(ExperimentalResourceApi::class)
        val painter = remember { CMP_SVG.encodeToByteArray().decodeToSvgPainter(density) }
        Image(painter, null, modifier = Modifier.size(64.dp))
        Spacer(Modifier.width(12.dp))

        // Animated gradient based on Alejandra Stamato's work:
        // https://medium.com/androiddevelopers/animating-brush-text-coloring-in-compose-%EF%B8%8F-26ae99d9b402
        val infiniteTransition = rememberInfiniteTransition()
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        val brush = remember(offset) {
            object : ShaderBrush() {
                override fun createShader(size: Size): Shader {
                    val widthOffset = size.width * offset
                    val heightOffset = size.height * offset
                    return LinearGradientShader(
                        colors = listOf(Color.Cyan, Color.Magenta),
                        from = Offset(widthOffset, heightOffset),
                        to = Offset(widthOffset + size.width, heightOffset + size.height),
                        tileMode = TileMode.Mirror
                    )
                }
            }
        }
        BasicText("Powered by Compose Multiplatform", style = TextStyle(fontSize = 36.sp, brush = brush))
    }
}

private val CMP_SVG =
    "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"55\" height=\"60\" fill=\"none\" viewBox=\"0 0 55 60\">\n" +
        "  <path fill=\"#4285F4\" fill-rule=\"evenodd\" d=\"M29.1884 2.15977a4.06229 4.06229 0 0 0-4.0525 0L3.64946 14.5271a4.04701 4.04701 0 0 0-.97135.78l9.00649 5.1616c.2173-.2351.4703-.4379.7549-.6033l13.0994-7.5745a3.24284 3.24284 0 0 1 1.6233-.4355c.5699 0 1.1298.1502 1.6232.4355l13.0987 7.5745c.2845.1646.5383.369.7556.6033l9.0065-5.1616a4.04618 4.04618 0 0 0-.9713-.78L29.1884 2.15977ZM52.4773 16.7 43.41 21.8957c.0616.2514.0941.5124.0941.7776v14.4276a3.2445 3.2445 0 0 1-.4246 1.6047c-.2784.489-.6792.8972-1.163 1.1844L28.753 47.7046a3.24107 3.24107 0 0 1-.78.3341v10.2405a4.05626 4.05626 0 0 0 1.1578-.4613l21.5838-12.7541a4.04297 4.04297 0 0 0 1.4561-1.477c.3486-.6105.532-1.3015.532-2.0046V18.0306c0-.4598-.0786-.9089-.2254-1.3306ZM1.62162 18.0306c0-.4598.07865-.9081.22541-1.3298l9.06727 5.1949c-.0616.2514-.094.5124-.094.7776v15.1078c0 .5819.1566 1.1531.4533 1.6537.2967.5006.7227.9121 1.2332 1.1915l13.0329 7.1351c.2595.1411.5319.2449.8117.3113v10.2471a4.05916 4.05916 0 0 1-1.23-.4322L3.73216 46.233a4.04322 4.04322 0 0 1-1.54262-1.4851 4.04366 4.04366 0 0 1-.56792-2.0646V18.0306ZM24.3259.75869a5.68713 5.68713 0 0 1 5.6725 0L51.4849 13.126a5.65951 5.65951 0 0 1 2.8394 4.9054V41.583a5.65961 5.65961 0 0 1-.7454 2.8054 5.6596 5.6596 0 0 1-2.0381 2.0668L29.957 59.21a5.68597 5.68597 0 0 1-2.7938.7896 5.68656 5.68656 0 0 1-2.8194-.6923L2.95459 47.6527a5.66005 5.66005 0 0 1-2.15983-2.0794A5.65995 5.65995 0 0 1 0 42.6825V18.0314a5.6593 5.6593 0 0 1 2.83946-4.9054L24.3259.75869ZM17.3497 23.0981l8.58-4.954c.7622-.4395 1.7027-.4395 2.4649 0l8.58 4.954c.3698.2126.6772.5189.8911.8879.2139.3691.3269.788.3275 1.2146v9.4484c0 .8562-.454 1.6491-1.1943 2.0886l-8.6294 5.1146a2.46794 2.46794 0 0 1-1.2143.3437c-.4277.0074-.85-.0965-1.2255-.3015L17.4 37.2322a2.42942 2.42942 0 0 1-.9267-.8909 2.42942 2.42942 0 0 1-.3422-1.2391v-9.9016c0-.866.4646-1.667 1.2186-2.1025Z\" clip-rule=\"evenodd\"/>\n" +
        "</svg>\n"
