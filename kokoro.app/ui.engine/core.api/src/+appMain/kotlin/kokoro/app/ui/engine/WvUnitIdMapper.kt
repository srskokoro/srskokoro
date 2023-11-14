package kokoro.app.ui.engine

fun interface WvUnitIdMapper {

	fun toWvUnitId(wvUnitKey: String): Int
}
