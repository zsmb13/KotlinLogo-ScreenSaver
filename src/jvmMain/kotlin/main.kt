import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import compose.ScreenSaverContent

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(width = 600.dp, height = 800.dp),
        ) {
            ScreenSaverContent(TODO(), TODO(), TODO(), TODO())
        }
    }
}
