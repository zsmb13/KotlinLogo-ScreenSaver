package config

import platform.Foundation.NSUserDefaults
import platform.Foundation.setValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object Preferences {
    var LOGO_SET by LongUserDefaultDelegate(0)
    var LOGO_SIZE by LongUserDefaultDelegate(200)
    var LOGO_COUNT by LongUserDefaultDelegate(1)
    var SPEED by LongUserDefaultDelegate(10)

    var CUSTOM_FOLDER by StringUserDefaultDelegate()

    const val IS_DEBUG = true
    const val APP_ID = "co.zsmb.KotlinLogos"

    internal fun reset() {
        LOGO_SET = 0
        LOGO_SIZE = 200
        LOGO_COUNT = 1
        SPEED = 10
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
