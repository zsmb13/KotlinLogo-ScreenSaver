@file:OptIn(ExperimentalForeignApi::class)

import appkit.NSImageLoader
import compose.ComposeAssetImageLoader
import compose.ComposeFolderImageLoader
import config.Preferences
import config.RenderMode
import imagesets.CustomFolderImageSet
import imagesets.ImageSet
import imagesets.ImageSetRepo
import imagesets.UImageLoader
import imagesets.add
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AppKit.NSApp
import platform.AppKit.NSBackingStoreBuffered
import platform.AppKit.NSColor
import platform.AppKit.NSWindow
import platform.AppKit.NSWindowStyleMaskClosable
import platform.AppKit.NSWindowStyleMaskMiniaturizable
import platform.AppKit.NSWindowStyleMaskResizable
import platform.AppKit.NSWindowStyleMaskTitled
import platform.Foundation.NSMakeRect
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalForeignApi::class)
fun main() {
    dynamicMainFun()
}

class MainParamProvider(private val imageSetRepo: ImageSetRepo) : ParamProvider {
    private var compose = true
    private val prefs
        get() = Preferences(
            logoSet = 0,
            logoSize = 100,
            logoCount = 5,
            speed = 15,
            customFolder = "/Users/zsmb/screensaver-images", // TODO edit custom folder here
            renderMode = RenderMode.Demo,
            debugMode = false,
        )

    override val params: ScreenSaverParams
        get() = ScreenSaverParams(
            logoSize = prefs.logoSize,
            logoCount = prefs.logoCount,
            speed = prefs.speed,
            useCompose = compose,
            demoMode = prefs.renderMode == RenderMode.Demo,
            debugMode = prefs.debugMode,
            imageSet = imageSetRepo.getCurrentImageSet()
        )

    private val callbacks = mutableListOf<(ScreenSaverParams) -> Unit>()

    override fun addCallback(cb: (ScreenSaverParams) -> Unit) {
        callbacks += cb
    }

    private fun notifyCallbacks(params: ScreenSaverParams) {
        callbacks.forEach { it(params) }
    }

    init {
        GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                delay(5.seconds)
                compose = !compose
                notifyCallbacks(params)
            }
        }
    }
}

class MainImageSetRepo(customFolder: String) : ImageSetRepo {
    private val imageSet = CustomFolderImageSet(customFolder)!!
    override fun getCurrentImageSet(): ImageSet = imageSet
    override fun getImageSets(): List<ImageSet> = listOf(getCurrentImageSet())
}

private fun dynamicMainFun() {
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
    window.center()
    window.makeKeyAndOrderFront(null)

    val contentView = requireNotNull(window.contentView)
    contentView.wantsLayer = true
    contentView.layer?.backgroundColor = NSColor.blackColor.CGColor

    val imgSetRepo = MainImageSetRepo("/Users/zsmb/screensaver-images")
    val prefParamProvider = MainParamProvider(imgSetRepo)
    val screenSpecs = ScreenSpecs(contentView)
    val imgLoader = UImageLoader(prefParamProvider, screenSpecs).apply {
        add(ComposeFolderImageLoader())
        add(ComposeAssetImageLoader())
        add(NSImageLoader())
    }

    val screenSaverView = DynamicScreenSaverView(prefParamProvider, imgSetRepo, imgLoader)


    screenSaverView.init(contentView, false)
    screenSaverView.startAnimation()
    GlobalScope.launch(Dispatchers.Main) {
        while (true) {
            delay(1000.milliseconds / 60.0)
            screenSaverView.animateOneFrame()
        }
    }

    NSApp?.run()
}
