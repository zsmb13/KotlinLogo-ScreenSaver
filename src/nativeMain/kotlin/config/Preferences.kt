package config

import platform.Foundation.NSUserDefaults
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object Preferences {
    var LOGO_SET by UserDefaultDelegate(0)
    var LOGO_SIZE by UserDefaultDelegate(200)
    var LOGO_COUNT by UserDefaultDelegate(1)
    var SPEED by UserDefaultDelegate(10)

    const val IS_DEBUG = false
}

class UserDefaultDelegate(private val default: Long) : ReadWriteProperty<Any?, Int> {
    private val userDefaults = NSUserDefaults()
    
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return ((userDefaults.objectForKey(property.name) as? Long) ?: default).toInt() 
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        userDefaults.setInteger(value.toLong(), property.name)
    }
}
