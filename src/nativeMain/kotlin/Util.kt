import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSColor
import platform.AppKit.NSTextAlignment
import platform.AppKit.NSTextField
import platform.AppKit.NSView
import platform.Foundation.NSRect

@OptIn(ExperimentalForeignApi::class)
fun addTextField(
    parent: NSView,
    string: String,
    alignment: NSTextAlignment,
    frame: CValue<NSRect>
): NSTextField {
    val textField = NSTextField(frame)
    textField.wantsLayer = true
    textField.drawsBackground = false
    textField.backgroundColor = NSColor.clearColor
    textField.textColor = NSColor.whiteColor
    textField.bezeled = false
    textField.editable = false
    textField.alignment = alignment
    textField.stringValue = string

    parent.addSubview(textField)

    return textField
}
