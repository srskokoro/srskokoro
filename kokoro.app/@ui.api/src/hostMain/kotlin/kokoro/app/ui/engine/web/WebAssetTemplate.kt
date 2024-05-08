package kokoro.app.ui.engine.web

import kotlin.jvm.JvmField

fun interface WebAssetTemplate {

	companion object {
		@JvmField val EMPTY = WebAssetTemplate()
	}

	suspend fun apply(request: WebRequest, spec: WebAssetSpec): WebResponse
}

/** @see WebAssetTemplate.EMPTY */
fun WebAssetTemplate(): WebAssetTemplate = EmptyWebAssetTemplate()

inline fun WebAssetTemplate(
	crossinline block: WebAssetSpec.(request: WebRequest) -> WebResponse,
) = WebAssetTemplate { request, spec -> spec.block(request) }

private class EmptyWebAssetTemplate : WebAssetTemplate {
	override suspend fun apply(request: WebRequest, spec: WebAssetSpec) = WebResponse(mimeType = "text/plain")
}
