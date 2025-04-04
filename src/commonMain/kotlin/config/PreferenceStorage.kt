package config

interface PreferenceStorage {
    fun getPreferences(): Preferences
    fun update(actions: Preferences.() -> Unit)
}
