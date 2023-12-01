package kokoro.app.ui.engine

import kotlin.jvm.JvmField

fun interface WvUnitIdMapper {

	fun toWvUnitId(wvUnitKey: String): Int

	companion object {
		@JvmField val NULL = WvUnitIdMapper { -1 }
	}
}
