import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import platform.AppKit.NSApp
import platform.AppKit.NSApplication
import util.debugLog

fun main() {
    println("Before mainx")

    NSApplication.sharedApplication()
    Window("Graphics2D") {
        var on by remember { mutableStateOf(false) }
        val color by animateColorAsState(if (on) Color.Red else Color.Green)

        val infiniteTransition = rememberInfiniteTransition()
        val animatedRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Box(Modifier.size(200.dp).background(Color.White)) {
            Box(
                Modifier
                    .graphicsLayer {
                        rotationY = animatedRotation
                        debugLog { "Rotation: $animatedRotation" }
                    }    .size(100.dp)
                    .background(color)
                    .clickable {
                        on = !on
                        debugLog { "clicked!" }
                    }

            )
        }
    }
    NSApp?.run()
}
