package imagesets

import ParamProvider
import ScreenSpecs
import util.debugLog
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.reflect.KClass

class UImageLoader(
    private val pp: ParamProvider,
    private val ss: ScreenSpecs,
) {
    private val loaders = mutableMapOf<KClass<*>, List<ImageLoader<*>>>()

    fun add(kclass: KClass<*>, loader: ImageLoader<*>) {
        loaders[kclass] = (loaders[kclass] ?: emptyList()) + loader
    }

    private val imageCache = mutableMapOf<KClass<*>, MutableMap<String, LoadedImage<*>>>()

    private val adjuster = { aspectRatio: Float ->
        val logoSize = pp.params.logoSize.toDouble()
        val scale = ss.pxScale
        val area = (logoSize * scale).pow(2).toFloat()

        val logoHeight = sqrt(area / aspectRatio)
        val logoWidth = area / logoHeight
        logoWidth to logoHeight
    }

    @Suppress("UNCHECKED_CAST")
    fun <I> loadImage(kclass: KClass<*>, imageSet: ImageSet, index: Int): LoadedImage<I>? {
        val cache = imageCache.getOrPut(kclass) { mutableMapOf() }

        val key = imageSet.images[index]
        val image = try {
            cache.getOrPut(key) {
                val loaders = loaders[kclass] ?: return null
                loaders.firstNotNullOfOrNull {
                    (it as ImageLoader<I>).loadImage(imageSet, index, adjuster)
                } as LoadedImage<I>
            } as LoadedImage<I>
        } catch (e: ClassCastException) {
            debugLog { "ERROR: Image loading failed! Tried to load $imageSet index $index, as type $kclass, using ${loaders[kclass]}" }
            return null
        }
        return image
    }
}

inline fun <reified I> UImageLoader.add(loader: ImageLoader<I>) {
    add(I::class, loader)
}

inline fun <reified I> UImageLoader.loadImage(imageSet: ImageSet, index: Int): LoadedImage<I>? {
    return loadImage(I::class, imageSet, index)
}
