package kokoro.app.ui.wv

interface WvSetup : WvUnitIdMapper {
	val wvJsAsset: String
	override fun wvUnitId(wvUnitKey: String): Int
}

inline fun WvSetup(
	wvJsAsset: String,
	crossinline wvUnitIdMapper: (wvUnitKey: String) -> Int,
) = object : WvSetup {
	override val wvJsAsset get() = wvJsAsset
	override fun wvUnitId(wvUnitKey: String) = wvUnitIdMapper.invoke(wvUnitKey)
}
