package kokoro.app.ui.wv

abstract class WvSetup(
	val wvJsAsset: String,
) : WvUnitIdMapper {
	abstract override fun invoke(wvUnitKey: String): Int
}

inline fun WvSetup(
	wvJsAsset: String,
	crossinline wvUnitIdMapper: WvUnitIdMapper,
) = object : WvSetup(wvJsAsset) {
	override fun invoke(wvUnitKey: String) = wvUnitIdMapper.invoke(wvUnitKey)
}
