package kokoro.app.ui.engine.web

import kotlin.jvm.JvmField

data class HtmlTitleSpec(
	@JvmField val title: String?,
) : WebAssetSpec {

	override fun query(key: String): String? =
		if (key == PROP_TITLE) title else null

	companion object {
		const val PROP_TITLE = "title"
	}
}
