package config

import ImageSet
import imageSets
import kotlinx.cinterop.*
import platform.AppKit.*
import platform.Foundation.NSMakeRect
import platform.Foundation.NSSelectorFromString
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty0

@OptIn(ExperimentalForeignApi::class)
class KotlinLogosPrefController : NSWindowController, NSWindowDelegateProtocol {
    public constructor(coder: platform.Foundation.NSCoder) : super(coder)

    private lateinit var sizeTextField: NSTextField
    private lateinit var speedTextField: NSTextField
    private lateinit var countTextField: NSTextField

    private lateinit var sizeStepper: NSStepper
    private lateinit var speedStepper: NSStepper
    private lateinit var countStepper: NSStepper

    private lateinit var setComboBox: NSComboBox

    constructor() : super(
        NSWindow(
            contentRect = NSMakeRect(x = 0.0, y = 0.0, w = 240.0, h = 200.0),
            styleMask = NSWindowStyleMaskClosable,
            backing = NSBackingStoreBuffered,
            defer = true
        )
    ) {
        val mainStack = NSStackView()
        mainStack.orientation = NSUserInterfaceLayoutOrientationVertical

        mainStack.addView(
            createComboBox(
                title = "Logo set",
                ::setComboBox,
            ), NSStackViewGravityTop
        )

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
                max = 50,
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

        val cancelButton = NSButton().apply {
            setListener(::performCancel)
            title = "Cancel"
            keyEquivalent = "\u001B"
            bezelStyle = NSBezelStyleRounded
        }
        buttonsStack.addView(cancelButton, NSStackViewGravityTrailing)

        val okButton = NSButton().apply {
            setListener(::performOk)
            title = "OK"
            keyEquivalent = "\r"
            bezelStyle = NSBezelStyleRounded
        }
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

    private fun NSControl.setListener(selfFunc: KFunction<Unit>) {
        target = this@KotlinLogosPrefController
        action = NSSelectorFromString(selfFunc.name)
    }

    private fun loadValuesFromPrefs() {
        setComboBox.selectItemAtIndex(Preferences.LOGO_SET.toLong())
        sizeStepper.setIntValue(Preferences.LOGO_SIZE)
        countStepper.setIntValue(Preferences.LOGO_COUNT)
        speedStepper.setIntValue(Preferences.SPEED)
    }

    private fun saveValuesToPrefs() {
        Preferences.LOGO_SET = setComboBox.indexOfSelectedItem.toInt()
        Preferences.LOGO_SIZE = sizeStepper.intValue
        Preferences.LOGO_COUNT = countStepper.intValue
        Preferences.SPEED = speedStepper.intValue
    }

    @ObjCAction
    fun updateDisplayedValues() {
        sizeTextField.stringValue = sizeStepper.intValue.toString()
        countTextField.stringValue = countStepper.intValue.toString()
        speedTextField.stringValue = speedStepper.intValue.toString()
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

fun prefController(): NSWindowController {
    return KotlinLogosPrefController()
}
