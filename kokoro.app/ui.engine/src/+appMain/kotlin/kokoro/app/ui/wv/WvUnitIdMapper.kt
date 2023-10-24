package kokoro.app.ui.wv

fun interface WvUnitIdMapper {
	fun wvUnitId(wvUnitKey: String): Int
}
