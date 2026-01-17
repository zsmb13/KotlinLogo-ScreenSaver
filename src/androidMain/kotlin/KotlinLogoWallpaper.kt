package co.zsmb.kotlinlogos

import ParamProvider
import ScreenSaverParams
import ScreenSpecs
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.creativedrewy.mozart.MozartWallpaperService
import compose.AndroidResImageLoader
import compose.ScreenSaverContent
import imagesets.AndroidResImageSet
import imagesets.UImageLoader
import imagesets.add

class KotlinLogoWallpaper: MozartWallpaperService() {

    companion object {
        lateinit var context: Context
    }

    init {
        context = this
    }

    override val wallpaperContents: @Composable ((OffsetValues) -> Unit)
        get() = { offsets ->
            Box(
                modifier = Modifier
            ) {
                val specs = remember(offsets) {
                    ScreenSpecs(
                        screenWidth = offsets.screenWidth.toDouble(),
                        screenHeight = offsets.screenHeight.toDouble(),
                        pxScale = 1.0,
                    )
                }

                val paramProvider = remember {
                    object : ParamProvider {
                        override val params: ScreenSaverParams = ScreenSaverParams(
                            logoSize = 100,
                            logoCount = 5,
                            speed = 20,
                            imageSet = AndroidResImageSet("Android", listOf("android")),
                            useCompose = true,
                            demoMode = true,
                            debugMode = false,
                        )

                        override fun addCallback(cb: (ScreenSaverParams) -> Unit) {}
                    }
                }

                val uImageLoader = remember(paramProvider, specs) {
                    UImageLoader(paramProvider, specs).apply {
                        add<Painter>(AndroidResImageLoader())
                    }
                }

                ScreenSaverContent(
                    params = paramProvider.params,
                    imgLoader = uImageLoader,
                    specs = specs,
                )
            }
        }
}
