package kokoro.app.ui.engine

import androidx.collection.ScatterMap
import androidx.collection.emptyScatterMap
import kotlin.jvm.JvmField

data class UiSpec(
	@JvmField val uiClass: String?,
	@JvmField val props: ScatterMap<String, String>,
) {
	constructor() : this(null)
	constructor(uiClass: String?) : this(uiClass, emptyScatterMap())
	constructor(props: ScatterMap<String, String>) : this(null, props)

	//  --

	fun prop(key: String): String = props[key] ?: throw E_NoSuchProp(key)

	@Suppress("NOTHING_TO_INLINE")
	inline fun propOrNull(key: String): String? = props[key]
}

private fun E_NoSuchProp(key: String) = NoSuchElementException("key=$key")
