package kokoro.app.ui.engine

import kokoro.app.ui.engine.web.WebAssetSpec
import kotlin.jvm.JvmField

open class UiSpec(
	@JvmField val uiFqn: String?,
) : WebAssetSpec {

	override fun toString(): String = "UiSpec($uiFqn)"

	override fun query(key: String): String? =
		if (key == PROP_UI) uiFqn else null

	companion object {
		const val PROP_UI = "ui"
	}
}
