package compose

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.drawable.toBitmap
import co.zsmb.kotlinlogos.KotlinLogoWallpaper
import co.zsmb.kotlinlogos.R
import imagesets.Adjuster
import imagesets.AndroidResImageSet
import imagesets.ImageLoader
import imagesets.ImageSet
import imagesets.LoadedImage

class AndroidResImageLoader : ImageLoader<Painter> {
    override fun loadImage(
        imageSet: ImageSet,
        index: Int,
        adjustSize: Adjuster,
    ): LoadedImage<Painter>? {
        if (imageSet !is AndroidResImageSet) return null

        val drawable: Drawable = KotlinLogoWallpaper.context.getDrawable(R.drawable.compose_multiplatform_icon)!!
        val bitmap = drawable.toBitmap().asImageBitmap()
        val painter = BitmapPainter(bitmap)

        val (w, h) = adjustSize(painter.intrinsicSize.width / painter.intrinsicSize.height)
        return LoadedImage(
            image = painter,
            width = w,
            height = h,
        )
    }
}
