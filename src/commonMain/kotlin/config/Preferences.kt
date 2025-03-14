package config

interface Preferences {
    var LOGO_SET : Int
    var LOGO_SIZE : Int
    var LOGO_COUNT : Int
    var SPEED : Int

    var CUSTOM_FOLDER : String

    fun reset()
}

val GlobalPreferences = Preferences()

expect fun Preferences(): Preferences
