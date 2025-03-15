package config

import platform.Foundation.NSUserDefaults
import platform.Foundation.setValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

actual fun Preferences(): Preferences = UserDefaultsPreferences

object UserDefaultsPreferences : Preferences {
    override var LOGO_SET by LongUserDefaultDelegate(0)
    override var LOGO_SIZE by LongUserDefaultDelegate(200)
    override var LOGO_COUNT by LongUserDefaultDelegate(1)
    override var SPEED by LongUserDefaultDelegate(10)
    override var USE_COMPOSE by BooleanUserDefaultDelegate(false)

    override var CUSTOM_FOLDER by StringUserDefaultDelegate()

    const val APP_ID = "co.zsmb.KotlinLogos"

    override fun reset() {
        LOGO_SET = 0
        LOGO_SIZE = 200
        LOGO_COUNT = 1
        SPEED = 10
        USE_COMPOSE = false
        CUSTOM_FOLDER = ""
    }
}

class LongUserDefaultDelegate(private val default: Long) : ReadWriteProperty<Any?, Int> {
    private val userDefaults = NSUserDefaults()

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return ((userDefaults.objectForKey(property.name) as? Long) ?: default).toInt()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        userDefaults.setInteger(value.toLong(), forKey = property.name)
    }
}

class StringUserDefaultDelegate : ReadWriteProperty<Any?, String> {
    private val userDefaults = NSUserDefaults()

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return (userDefaults.objectForKey(property.name) as? String) ?: ""
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        userDefaults.setValue(value, forKey = property.name)
    }
}

class BooleanUserDefaultDelegate(private val default: Boolean) : ReadWriteProperty<Any?, Boolean> {
    private val userDefaults = NSUserDefaults()

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return (userDefaults.objectForKey(property.name) as? Boolean) ?: default
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        userDefaults.setBool(value, forKey = property.name)
    }
}
