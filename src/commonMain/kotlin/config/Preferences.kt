package config

data class Preferences(
    var logoSet: Int,
    var logoSize: Int,
    var logoCount: Int,
    var speed: Int,
    var renderMode: RenderMode,
    var customFolder: String,
    var debugMode: Boolean,
)

enum class RenderMode { AppKit, Compose, Demo }
