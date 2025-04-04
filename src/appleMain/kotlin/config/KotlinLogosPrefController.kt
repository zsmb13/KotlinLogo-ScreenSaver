package config

import imagesets.AssetImageSet
import imagesets.CustomFolderImageSet
import imagesets.ImageSet
import imagesets.ImageSetRepo
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.copy
import platform.AppKit.NSBackingStoreBuffered
import platform.AppKit.NSBezelStyleRounded
import platform.AppKit.NSButton
import platform.AppKit.NSButtonTypeSwitch
import platform.AppKit.NSComboBox
import platform.AppKit.NSControl
import platform.AppKit.NSControlStateValueOff
import platform.AppKit.NSControlStateValueOn
import platform.AppKit.NSLayoutConstraint
import platform.AppKit.NSModalResponseOK
import platform.AppKit.NSOpenPanel
import platform.AppKit.NSStackView
import platform.AppKit.NSStackViewGravityCenter
import platform.AppKit.NSStackViewGravityTop
import platform.AppKit.NSStackViewGravityTrailing
import platform.AppKit.NSStepper
import platform.AppKit.NSTextField
import platform.AppKit.NSUserInterfaceLayoutOrientationVertical
import platform.AppKit.NSWindow
import platform.AppKit.NSWindowController
import platform.AppKit.NSWindowDelegateProtocol
import platform.AppKit.NSWindowStyleMaskClosable
import platform.AppKit.addView
import platform.AppKit.heightAnchor
import platform.AppKit.translatesAutoresizingMaskIntoConstraints
import platform.AppKit.widthAnchor
import platform.Foundation.NSMakeRect
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import util.debugLog
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty0

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class KotlinLogosPrefController(
    private val prefs: UserDefaultsPreferences,
    private val imageSetRepo: ImageSetRepo,
) : NSWindowController(
    NSWindow(
        contentRect = NSMakeRect(x = 0.0, y = 0.0, w = 320.0, h = 200.0),
        styleMask = NSWindowStyleMaskClosable,
        backing = NSBackingStoreBuffered,
        defer = true
    )
), NSWindowDelegateProtocol {
    private lateinit var sizeTextField: NSTextField
    private lateinit var speedTextField: NSTextField
    private lateinit var countTextField: NSTextField

    private lateinit var sizeStepper: NSStepper
    private lateinit var speedStepper: NSStepper
    private lateinit var countStepper: NSStepper

    private lateinit var setComboBox: NSComboBox
    private lateinit var renderModeComboBox: NSComboBox
    private lateinit var debugModeCheckbox: NSButton
    private var transientCustomFolder: String = ""

    init {
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

        val renderModeStack = createComboBox(title = "Renderer", ::renderModeComboBox)
        renderModeComboBox.removeAllItems()
        renderModeComboBox.addItemsWithObjectValues(listOf("AppKit", "Compose", "Demo mode"))
        mainStack.addView(renderModeStack, NSStackViewGravityTop)

        mainStack.addView(createCheckbox("Debug Mode", ::debugModeCheckbox), NSStackViewGravityTop)

        mainStack.addView(createButtonStack(), NSStackViewGravityTrailing)
        mainStack.setEdgeInsets(
            mainStack.edgeInsets.copy {
                top = 16.0
                bottom = 16.0
            }
        )

        requireNotNull(window).contentView = mainStack

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
                transientCustomFolder = url.toString()
                updateDisplayedValues()
                if (transientCustomFolder.isNotEmpty()) {
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

        val imageSets = imageSetRepo.getImageSets()
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

    private fun createCheckbox(title: String, checkboxProp: KMutableProperty0<NSButton>): NSStackView {
        val stack = NSStackView()
        stack.translatesAutoresizingMaskIntoConstraints = false

        val checkbox = NSButton().apply {
            this.title = title
            this.setButtonType(NSButtonTypeSwitch)
            this.state = NSControlStateValueOff
        }
        checkboxProp.set(checkbox)

        stack.addView(checkbox, NSStackViewGravityCenter)

        return stack
    }

    private fun NSControl.setListener(selfFunc: KFunction<Unit>) {
        target = this@KotlinLogosPrefController
        action = NSSelectorFromString(selfFunc.name)
    }

    private fun loadValuesFromPrefs() {
        val prefs = prefs.getPreferences()

        setComboBox.apply {
            removeAllItems()
            addItemsWithObjectValues(imageSetRepo.getImageSets().map(ImageSet::name))
            selectItemAtIndex(prefs.logoSet.toLong())
        }
        transientCustomFolder = prefs.customFolder

        sizeStepper.setIntValue(prefs.logoSize)
        countStepper.setIntValue(prefs.logoCount)
        speedStepper.setIntValue(prefs.speed)

        renderModeComboBox.selectItemAtIndex(prefs.renderMode.ordinal.toLong())
        debugModeCheckbox.state = if (prefs.debugMode) NSControlStateValueOn else NSControlStateValueOff
    }

    private fun saveValuesToPrefs() {
        prefs.update {
            if (transientCustomFolder.isNotEmpty()) {
                val custom = CustomFolderImageSet(transientCustomFolder)
                if (custom == null) {
                    // We had a custom folder set, but it fails to load, drop it
                    customFolder = ""
                    logoSet = 0
                } else {
                    // Successfully loaded custom folder, remember it and proceed as normal
                    customFolder = transientCustomFolder
                    logoSet = setComboBox.indexOfSelectedItem.toInt()
                }
            } else {
                logoSet = setComboBox.indexOfSelectedItem.toInt()
            }

            logoSize = sizeStepper.intValue
            logoCount = countStepper.intValue
            speed = speedStepper.intValue
            renderMode = when (renderModeComboBox.indexOfSelectedItem.toInt()) {
                0 -> RenderMode.AppKit
                1 -> RenderMode.Compose
                2 -> RenderMode.Demo
                else -> RenderMode.AppKit
            }
            debugMode = debugModeCheckbox.state == NSControlStateValueOn
        }
    }

    @ObjCAction
    fun updateDisplayedValues() {
        setComboBox.apply {
            val tempIndex = indexOfSelectedItem
            val customOption = transientCustomFolder.trimEnd('/').substringAfterLast("/")
            removeAllItems()

            val updatedItems = buildList {
                val imageSets = imageSetRepo.getImageSets()
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
        prefs.reset()
        loadValuesFromPrefs()
        window?.let { it.sheetParent?.endSheet(it) }
    }

    @ObjCAction
    fun performCancel() {
        window?.let { it.sheetParent?.endSheet(it) }
    }

    @ObjCAction
    fun performOk() {
        saveValuesToPrefs()
        window?.let { it.sheetParent?.endSheet(it) }
    }
}
