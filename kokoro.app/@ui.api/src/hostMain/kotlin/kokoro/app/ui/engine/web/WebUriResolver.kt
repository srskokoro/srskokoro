package kokoro.app.ui.engine.web

import kotlin.jvm.JvmField

fun interface WebUriResolver {

	fun resolve(uri: WebUri): WebResource?

	companion object {
		@JvmField val NULL: WebUriResolver = NullWebUriResolver
	}
}

operator fun WebUriResolver.plus(other: WebUriResolver): WebUriResolver = CombinedWebUriResolver(this, other)

// --

private data object NullWebUriResolver : WebUriResolver {
	override fun resolve(uri: WebUri): WebResource? = null
}

private data class CombinedWebUriResolver(
	private val a: WebUriResolver,
	private val b: WebUriResolver,
) : WebUriResolver {
	override fun resolve(uri: WebUri): WebResource? =
		a.resolve(uri) ?: b.resolve(uri)
}
