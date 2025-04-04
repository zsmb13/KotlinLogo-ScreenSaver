interface ScreenSaverImpl {
    fun animateOneFrame() {}
    fun start(params: ScreenSaverParams)
    fun dispose()
    fun prefsChanged(params: ScreenSaverParams)
}
