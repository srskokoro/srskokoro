package kokoro.app.ui.engine.window

import kokoro.internal.DEBUG
import kokoro.internal.require
import kotlin.jvm.JvmField

data class WvWindowId(
	@JvmField val name: String,
	val factoryId: WvWindowFactoryId,
) {
	init {
		if (DEBUG) require(WvWindowFactory.get(factoryId) != null, or = {
			"Window factory ID not registered: $factoryId"
		})
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun checkOnLaunch() {
		if (DEBUG) require(!factoryId.isNothing, or = {
			"Window factory ID cannot be used to launch window: $factoryId"
		})
	}

	override fun toString(): String = "$name|$factoryId"
}
