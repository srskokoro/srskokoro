package kokoro.app.ui.wv

interface WvSetup : WvUnitIdMapper {
	val wvJsAsset: String
	override fun invoke(wvUnitKey: String): Int
}

inline fun WvSetup(
	wvJsAsset: String,
	crossinline wvUnitIdMapper: WvUnitIdMapper,
) = object : WvSetup {
	override val wvJsAsset get() = wvJsAsset
	override fun invoke(wvUnitKey: String) = wvUnitIdMapper.invoke(wvUnitKey)
}
