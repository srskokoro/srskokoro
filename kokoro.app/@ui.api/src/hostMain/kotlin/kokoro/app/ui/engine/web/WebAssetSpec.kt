package kokoro.app.ui.engine.web

import androidx.collection.ScatterMap
import kotlin.jvm.JvmField

fun interface WebAssetSpec {

	companion object {
		@JvmField val EMPTY = WebAssetSpec()
	}

	fun propOrNull(key: String): String?
}

fun WebAssetSpec.prop(key: String): String = propOrNull(key) ?: throw E_NoSuchProp(key)

private fun E_NoSuchProp(key: String) = NoSuchElementException("key=$key")

// --

/** @see WebAssetSpec.EMPTY */
fun WebAssetSpec(): WebAssetSpec = EmptyWebAssetSpec()

fun WebAssetSpec(map: Map<in String, String?>): WebAssetSpec = MapWebAssetSpec(map)

fun WebAssetSpec(map: ScatterMap<in String, out String?>): WebAssetSpec = ScatterMapWebAssetSpec(map)

// --

private class EmptyWebAssetSpec : WebAssetSpec {
	override fun propOrNull(key: String): String? = null
}

private class MapWebAssetSpec(
	private val map: Map<in String, String?>,
) : WebAssetSpec {

	override fun propOrNull(key: String): String? = map[key]

	override fun toString() = "${super.toString()}(map=$map)"
}

private class ScatterMapWebAssetSpec(
	private val map: ScatterMap<in String, out String?>,
) : WebAssetSpec {

	override fun propOrNull(key: String): String? = map[key]

	override fun toString() = "${super.toString()}(map=$map)"
}
