package imagesets

import util.debugLog
import java.io.File

private fun File.isSupportedImage(): Boolean =
    name.substringAfterLast('.', "").lowercase() in setOf(
        //"png",
        "svg")

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

    val folder = File(folderUrl)
    if (!folder.exists() || !folder.isDirectory) {
        debugLog { "Folder does not exist or is not a directory: $folderUrl" }
        return null
    }

    val files = folder.listFiles() ?: emptyArray()
    var fileCount = files.size
    val images = files
        .filter { it.isFile && it.isSupportedImage() }
        .map { it.absolutePath }
        .sorted()

    debugLog { "Inspected $fileCount files, found ${images.size} images" }
    debugLog { images.joinToString() }

    if (images.isEmpty()) {
        debugLog { "Failed to create custom image set" }
        return null
    }

    return CustomFolderImageSet(
        name = folder.name,
        images = images,
    )
}
