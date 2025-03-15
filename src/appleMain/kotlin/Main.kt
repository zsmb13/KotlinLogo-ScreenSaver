@file:OptIn(ExperimentalForeignApi::class)

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import appkit.AppKitScreenSaverView
import compose.ComposeImageLoader
import compose.ComposeScreenSaverView
import compose.PrefValues
import compose.ScreenSaverContent
import config.GlobalPreferences
import imagesets.CustomFolderImageSet
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AppKit.NSApp
import platform.AppKit.NSBackingStoreBuffered
import platform.AppKit.NSView
import platform.AppKit.NSWindow
import platform.AppKit.NSWindowStyleMaskClosable
import platform.AppKit.NSWindowStyleMaskMiniaturizable
import platform.AppKit.NSWindowStyleMaskResizable
import platform.AppKit.NSWindowStyleMaskTitled
import platform.Foundation.NSMakeRect
import util.debugLog
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalForeignApi::class)
fun main() {
    dynamicMainFun()
}

var activeImpl: ScreenSaverImpl? = null
var composeImpl: Boolean = false

private fun dynamicMainFun() {
    GlobalPreferences.LOGO_SET = 2
    GlobalPreferences.CUSTOM_FOLDER = "/Users/zsmb/screensaver-images"

    val window = NSWindow(
        contentRect = NSMakeRect(x = 0.0, y = 0.0, w = 800.0, h = 600.0),
        styleMask = NSWindowStyleMaskTitled or
            NSWindowStyleMaskMiniaturizable or
            NSWindowStyleMaskClosable or
            NSWindowStyleMaskResizable,
        backing = NSBackingStoreBuffered,
        defer = true
    )
    window.title = "Dynamic screen saver"
    val contentView = window.contentView!!

//    activeImpl = AppKitScreenSaverView(contentView, false)
//    contentView.addSubview(activeImpl!!.view)
    setImpl(contentView)

    window.center()
    window.makeKeyAndOrderFront(null)

    GlobalScope.launch(Dispatchers.Main) {
        while (true) {
            delay(1000.milliseconds / 60)
            activeImpl?.animateOneFrame()
        }
    }

    GlobalScope.launch(Dispatchers.Main) {
        while (true) {
            delay(5.seconds)
            composeImpl = !composeImpl
            setImpl(contentView)
        }
    }

    NSApp?.run()
}

private fun setImpl(contentView: NSView) {
    val disposing = activeImpl
    activeImpl = null

    // Clean up old implementation if it exists
    disposing?.let { impl ->
        debugLog { "Cleaning up old implementation: ${impl::class.simpleName}" }
        // Additional cleanup if needed
        impl.dispose()
    }

    // Create and initialize new implementation
    activeImpl = if (composeImpl) {
        debugLog { "Switching to Compose implementation" }
        ComposeScreenSaverView(contentView, show = false)
    } else {
        debugLog { "Switching to AppKit implementation" }
        AppKitScreenSaverView(contentView)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun composeMainFun() {
    Window("KotlinLogo macOS native") {
        val specs = remember {
            ScreenSpecs(
                screenWidth = this.window.contentLayoutRect.useContents { this.size.width },
                screenHeight = this.window.contentLayoutRect.useContents { this.size.height },
                pxScale = 2.0,
            )
        }

        var prefs by remember {
            mutableStateOf(
                PrefValues(
                    logoSize = 50,
                    logoSet = -1, // unused
                    logoCount = 1,
                    speed = 10,
                )
            )
        }

        ScreenSaverContent(
            prefs = prefs,
            imageSet = CustomFolderImageSet("/Users/zsmb/screensaver-images")!!,
            imgLoader = ComposeImageLoader(
                LocalDensity.current,
                targetArea = (prefs.logoSize * specs.pxScale).pow(2).toFloat()
            ),
            specs = specs,
            onClick = {
                val newpref = prefs.copy(logoSize = prefs.logoSize + 10)
                println("new pref is $newpref")
                prefs = newpref
                println("new pref set!")
            }
        )
    }

    NSApp?.run()
}
