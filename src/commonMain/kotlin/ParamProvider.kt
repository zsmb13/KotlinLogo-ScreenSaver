interface ParamProvider {
    val params: ScreenSaverParams
    fun addCallback(cb: (ScreenSaverParams) -> Unit)
}
