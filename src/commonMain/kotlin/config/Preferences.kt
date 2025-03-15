package config

interface Preferences {
    var LOGO_SET : Int
    var LOGO_SIZE : Int
    var LOGO_COUNT : Int
    var SPEED : Int
    var USE_COMPOSE : Boolean

    var CUSTOM_FOLDER : String

    fun reset()
}

const val DEBUG_MODE = true

val GlobalPreferences = Preferences()

expect fun Preferences(): Preferences
