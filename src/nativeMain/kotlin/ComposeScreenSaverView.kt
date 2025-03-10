//import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import platform.AppKit.NSView
import platform.ScreenSaver.ScreenSaverView
import util.debugLog

class ComposeScreenSaverView : KotlinScreenSaverView() {
    override fun init(screenSaverView: ScreenSaverView, isPreview: Boolean) {
        debugLog { "ComposeScreenSaverView initing, isPreview: $isPreview" }
        super.init(screenSaverView, isPreview)
        attach(screenSaverView)
        debugLog { "ComposeScreenSaverView inited, isPreview: $isPreview" }
    }

    override fun animateOneFrame() {
        debugLog { "animateOneFrame" }
    }
}

fun attach(screenSaverView: ScreenSaverView) {
    debugLog { "attempting to attach" }

//    NSApplication.sharedApplication()
    var view: NSView? = null
    Window("Graphics2D") {
//        BasicText("Hello")
        view = this.window.contentView

        var on by remember { mutableStateOf(false) }
        val color by animateColorAsState(if (on) Color.Red else Color.Green)
        Box(
            Modifier
                .size(100.dp)
                .background(color)
                .clickable {
                    on = !on
                    debugLog { "clicked!" }
                }
        )
    }
    println("view is $view")
    debugLog { "view is $view" }
    view?.let {
        screenSaverView.addSubview(it)
    }
//    NSApp?.run()
}



