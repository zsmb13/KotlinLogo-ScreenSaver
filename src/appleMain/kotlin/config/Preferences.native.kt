package config

import platform.Foundation.NSUserDefaults
import platform.Foundation.setValue
import util._debugLoggingEnabled
import util.debugLog
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

const val APP_ID = "co.zsmb.KotlinLogos"

object UserDefaultsPreferences : PreferenceStorage {
    private var LOGO_SET by LongUserDefaultDelegate(0)
    private var LOGO_SIZE by LongUserDefaultDelegate(200)
    private var LOGO_COUNT by LongUserDefaultDelegate(1)
    private var SPEED by LongUserDefaultDelegate(10)
    private var RENDER_MODE by LongUserDefaultDelegate(0)
    private var DEBUG_MODE by BooleanUserDefaultDelegate(false)
    private var CUSTOM_FOLDER by StringUserDefaultDelegate()

    fun reset() {
        debugLog { "Resetting preferences" }
        setPreference(
            Preferences(
                logoSet = 0,
                logoSize = 200,
                logoCount = 1,
                speed = 10,
                renderMode = RenderMode.AppKit,
                customFolder = "",
                debugMode = false,
            )
        )
    }

    override fun getPreferences(): Preferences {
        return Preferences(
            logoSize = LOGO_SIZE,
            logoSet = LOGO_SET,
            logoCount = LOGO_COUNT,
            speed = SPEED,
            renderMode = RenderMode.entries[RENDER_MODE],
            customFolder = CUSTOM_FOLDER,
            debugMode = DEBUG_MODE,
        ).also {
            // TODO this could be connected in a nicer way
            _debugLoggingEnabled = it.debugMode
        }
    }

    private fun setPreference(preferences: Preferences) {
        debugLog { "Setting preferences $preferences" }
        LOGO_SIZE = preferences.logoSize
        LOGO_SET = preferences.logoSet
        LOGO_COUNT = preferences.logoCount
        SPEED = preferences.speed
        RENDER_MODE = preferences.renderMode.ordinal
        DEBUG_MODE = preferences.debugMode
        CUSTOM_FOLDER = preferences.customFolder
    }

    override fun update(actions: Preferences.() -> Unit) {
        setPreference(getPreferences().apply(actions))
    }
}

private class LongUserDefaultDelegate(private val default: Long) : ReadWriteProperty<Any?, Int> {
    private val userDefaults = NSUserDefaults()

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return ((userDefaults.objectForKey(property.name) as? Long) ?: default).toInt()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        userDefaults.setInteger(value.toLong(), forKey = property.name)
    }
}

private class StringUserDefaultDelegate : ReadWriteProperty<Any?, String> {
    private val userDefaults = NSUserDefaults()

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return (userDefaults.objectForKey(property.name) as? String) ?: ""
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        userDefaults.setValue(value, forKey = property.name)
    }
}

private class BooleanUserDefaultDelegate(private val default: Boolean) : ReadWriteProperty<Any?, Boolean> {
    private val userDefaults = NSUserDefaults()

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return (userDefaults.objectForKey(property.name) as? Boolean) ?: default
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        userDefaults.setBool(value, forKey = property.name)
    }
}
