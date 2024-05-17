package imagesets

import platform.AppKit.NSImage
import platform.Foundation.NSDirectoryEnumerationSkipsHiddenFiles
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import util.debugLog

class CustomFolderImageSet private constructor(
    override val name: String,
    override val images: List<String>,
) : ImageSet {
    override val size: Int = images.size

    override fun load(image: String): NSImage = NSImage(contentsOfURL = NSURL(string = image))

    companion object {
        fun load(folderUrl: String): CustomFolderImageSet? {
            debugLog { "Loading custom folder '$folderUrl'" }

            if (folderUrl.isEmpty()) {
                debugLog { "Empty custom folder URL" }
                return null
            }
            if (folderUrl.isBlank()) {
                debugLog { "Blank custom folder URL" }
                return null
            }

            val enum = NSFileManager.defaultManager.enumeratorAtURL(
                NSURL(string = folderUrl),
                null,
                NSDirectoryEnumerationSkipsHiddenFiles,
                null
            )
            val images = buildList {
                if (enum != null) {
                    var next = enum.nextObject()
                    while (next != null) {
                        if (next is NSURL && next.toString().substringAfterLast('.') in setOf("png", "svg")) {
                            add(next.toString())
                        }
                        next = enum.nextObject()
                    }
                }
            }

            debugLog { "Found ${images.size} images: $images" }

            if (images.isEmpty()) {
                debugLog { "Failed to create custom image set" }
                return null
            }

            return CustomFolderImageSet(
                name = folderUrl.trimEnd('/').substringAfterLast('/'),
                images = images.sorted(),
            )
        }
    }
}
