package imagesets

import platform.Foundation.NSDirectoryEnumerationSkipsHiddenFiles
import platform.Foundation.NSDirectoryEnumerationSkipsSubdirectoryDescendants
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import util.debugLog

actual fun CustomFolderImageSet(folderUrl: String): CustomFolderImageSet? {
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
        NSDirectoryEnumerationSkipsHiddenFiles or NSDirectoryEnumerationSkipsSubdirectoryDescendants,
        null
    )

    var fileCount = 0
    val images: List<String> = buildList {
        if (enum != null) {
            var next = enum.nextObject()
            while (next != null) {
                fileCount++
                if (next is NSURL && next.toString().isSupportedFormat()) {
                    add(next.toString())
                }
                next = enum.nextObject()
            }
        }
    }

    debugLog { "Inspected $fileCount files, found ${images.size} images" }
    debugLog { images.joinToString() }

    if (images.isEmpty()) {
        debugLog { "Failed to create custom image set" }
        return null
    }

    return CustomFolderImageSet(
        name = folderUrl.trimEnd('/').substringAfterLast('/'),
        images = images.sorted(),
    )
}
