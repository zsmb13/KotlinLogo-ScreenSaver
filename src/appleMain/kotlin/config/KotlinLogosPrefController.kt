package config

import imagesets.AssetImageSet
import imagesets.CustomFolderImageSet
import imagesets.ImageSet
import imagesets.imageSets
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.copy
import platform.AppKit.*
import platform.Foundation.NSMakeRect
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import util.debugLog
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty0

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class KotlinLogosPrefController : NSWindowController, NSWindowDelegateProtocol {
    private lateinit var sizeTextField: NSTextField
    private lateinit var speedTextField: NSTextField
    private lateinit var countTextField: NSTextField

    private lateinit var sizeStepper: NSStepper
    private lateinit var speedStepper: NSStepper
    private lateinit var countStepper: NSStepper

    private lateinit var setComboBox: NSComboBox
    private var customFolder: String = ""

    constructor() : super(
        NSWindow(
            contentRect = NSMakeRect(x = 0.0, y = 0.0, w = 320.0, h = 200.0),
            styleMask = NSWindowStyleMaskClosable,
            backing = NSBackingStoreBuffered,
            defer = true
        )
    ) {
        val mainStack = NSStackView()
        mainStack.orientation = NSUserInterfaceLayoutOrientationVertical

        val comboStack = createComboBox(
            title = "Logo set",
            ::setComboBox,
        )
        comboStack.addView(createButton("Browse", "x", ::openPicker), NSStackViewGravityCenter)
        mainStack.addView(comboStack, NSStackViewGravityTop)

        mainStack.addView(
            createStepper(
                title = "Size",
                listener = ::updateDisplayedValues,
                min = 50,
                max = 400,
                step = 25,
                ::sizeTextField,
                ::sizeStepper
            ), NSStackViewGravityTop
        )
        mainStack.addView(
            createStepper(
                title = "Count",
                listener = ::updateDisplayedValues,
                min = 1,
                max = 100,
                step = 1,
                ::countTextField,
                ::countStepper
            ), NSStackViewGravityTop
        )
        mainStack.addView(
            createStepper(
                title = "Speed",
                listener = ::updateDisplayedValues,
                min = 1,
                max = 25,
                step = 1,
                ::speedTextField,
                ::speedStepper
            ), NSStackViewGravityTop
        )

        mainStack.addView(createButtonStack(), NSStackViewGravityTrailing)
        mainStack.setEdgeInsets(
            mainStack.edgeInsets.copy {
                top = 16.0
                bottom = 16.0
            }
        )

        window!!.contentView = mainStack

        loadValuesFromPrefs()
        updateDisplayedValues()
    }

    private val openPanel by lazy {
        NSOpenPanel().apply {
            allowsMultipleSelection = false
            canChooseDirectories = true
            canCreateDirectories = false
            canChooseFiles = false
        }
    }

    @ObjCAction
    private fun openPicker() {
        openPanel.beginWithCompletionHandler { response ->
            debugLog { "Picker result was $response, ${openPanel.URLs}" }
            if (response == NSModalResponseOK && openPanel.URLs.isNotEmpty()) {
                val url = openPanel.URLs.first() as NSURL
                customFolder = url.toString()
                updateDisplayedValues()
                if (customFolder.isNotEmpty()) {
                    setComboBox.selectItemAtIndex(setComboBox.numberOfItems - 1)
                } else {
                    setComboBox.selectItemAtIndex(0)
                }
            }
        }
    }

    private fun createComboBox(
        title: String,
        comboBoxProp: KMutableProperty0<NSComboBox>,
    ): NSStackView {
        val stack = NSStackView()
        stack.translatesAutoresizingMaskIntoConstraints = false

        val label = NSTextField().apply {
            stringValue = title
            editable = false
            selectable = false
            drawsBackground = false
            bezeled = false
        }

        val comboBox = NSComboBox().apply {
            addItemsWithObjectValues(imageSets.map(ImageSet::name))
            editable = false
        }
        comboBoxProp.set(comboBox)

        stack.addView(label, NSStackViewGravityCenter)
        stack.addView(comboBox, NSStackViewGravityCenter)

        NSLayoutConstraint.activateConstraints(
            listOf(comboBox.widthAnchor.constraintEqualToConstant(120.0))
        )

        return stack
    }

    private fun createStepper(
        title: String,
        listener: KFunction<Unit>,
        min: Int,
        max: Int,
        step: Int,
        textFieldProp: KMutableProperty0<NSTextField>,
        stepperProp: KMutableProperty0<NSStepper>,
    ): NSStackView {
        val stack = NSStackView()
        stack.translatesAutoresizingMaskIntoConstraints = false

        val label = NSTextField().apply {
            stringValue = title
            editable = false
            selectable = false
            drawsBackground = false
            bezeled = false
        }

        val textField = NSTextField().apply {
            editable = false
            selectable = false
        }
        textFieldProp.set(textField)

        val stepper = NSStepper().apply {
            setListener(listener)
            minValue = min.toDouble()
            maxValue = max.toDouble()
            increment = step.toDouble()
            valueWraps = false
        }
        stepperProp.set(stepper)

        stack.addView(label, NSStackViewGravityCenter)
        stack.addView(textField, NSStackViewGravityCenter)
        stack.addView(stepper, NSStackViewGravityCenter)

        NSLayoutConstraint.activateConstraints(
            listOf(textField.widthAnchor.constraintEqualToConstant(75.0))
        )

        return stack
    }

    private fun createButtonStack(): NSStackView {
        val buttonsStack = NSStackView()

        val cancelButton = createButton("Cancel", "\u001B", ::performCancel)
        buttonsStack.addView(cancelButton, NSStackViewGravityTrailing)

        val resetButton = createButton("Reset", "R", ::performReset)
        buttonsStack.addView(resetButton, NSStackViewGravityTrailing)

        val okButton = createButton(title = "OK", keyEquivalent = "\r", action = ::performOk)
        buttonsStack.addView(okButton, NSStackViewGravityTrailing)

        NSLayoutConstraint.activateConstraints(
            listOf(
                okButton.widthAnchor.constraintEqualToConstant(75.0),
                okButton.heightAnchor.constraintEqualToConstant(25.0),
                cancelButton.widthAnchor.constraintEqualToConstant(75.0),
                cancelButton.heightAnchor.constraintEqualToConstant(25.0),
            )
        )

        return buttonsStack
    }

    private fun createButton(title: String, keyEquivalent: String, action: KFunction<Unit>): NSButton {
        return NSButton().apply {
            setListener(action)
            this.title = title
            this.keyEquivalent = keyEquivalent
            this.bezelStyle = NSBezelStyleRounded
        }
    }

    private fun NSControl.setListener(selfFunc: KFunction<Unit>) {
        target = this@KotlinLogosPrefController
        action = NSSelectorFromString(selfFunc.name)
    }

    private fun loadValuesFromPrefs() {
        setComboBox.apply {
            removeAllItems()
            addItemsWithObjectValues(imageSets.map(ImageSet::name))
            selectItemAtIndex(GlobalPreferences.LOGO_SET.toLong())
        }
        customFolder = GlobalPreferences.CUSTOM_FOLDER

        sizeStepper.setIntValue(GlobalPreferences.LOGO_SIZE)
        countStepper.setIntValue(GlobalPreferences.LOGO_COUNT)
        speedStepper.setIntValue(GlobalPreferences.SPEED)
    }

    private fun saveValuesToPrefs() {
        imageSets.retainAll { it is AssetImageSet }

        if (customFolder.isNotEmpty()) {
            val custom = CustomFolderImageSet(customFolder)
            if (custom == null) {
                // We had a custom folder set, but it fails to load, drop it
                GlobalPreferences.CUSTOM_FOLDER = ""
                GlobalPreferences.LOGO_SET = 0
            } else {
                // Successfully loaded custom folder, remember it and proceed as normal
                imageSets.add(custom)
                GlobalPreferences.CUSTOM_FOLDER = customFolder
                GlobalPreferences.LOGO_SET = setComboBox.indexOfSelectedItem.toInt()
            }
        } else {
            GlobalPreferences.LOGO_SET = setComboBox.indexOfSelectedItem.toInt()
        }

        GlobalPreferences.LOGO_SIZE = sizeStepper.intValue
        GlobalPreferences.LOGO_COUNT = countStepper.intValue
        GlobalPreferences.SPEED = speedStepper.intValue
    }

    @ObjCAction
    fun updateDisplayedValues() {
        setComboBox.apply {
            val tempIndex = indexOfSelectedItem
            val customOption = customFolder.trimEnd('/').substringAfterLast("/")
            removeAllItems()

            val updatedItems = buildList {
                addAll(imageSets.filterIsInstance<AssetImageSet>().map(ImageSet::name))

                if (customOption.isNotEmpty()) {
                    add(customOption)
                }
            }

            addItemsWithObjectValues(updatedItems)
            selectItemAtIndex(if (tempIndex in updatedItems.indices) tempIndex else 0)
        }
        sizeTextField.stringValue = sizeStepper.intValue.toString()
        countTextField.stringValue = countStepper.intValue.toString()
        speedTextField.stringValue = speedStepper.intValue.toString()
    }

    @ObjCAction
    fun performReset() {
        GlobalPreferences.reset()
        loadValuesFromPrefs()
        window?.sheetParent?.endSheet(window!!)
    }

    @ObjCAction
    fun performCancel() {
        window?.sheetParent?.endSheet(window!!)
    }

    @ObjCAction
    fun performOk() {
        saveValuesToPrefs()
        window?.sheetParent?.endSheet(window!!)
    }
}
