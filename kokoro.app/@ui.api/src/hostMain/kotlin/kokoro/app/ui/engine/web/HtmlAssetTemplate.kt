package kokoro.app.ui.engine.web

import androidx.annotation.EmptySuper
import kokoro.internal.DEBUG
import kokoro.internal.io.asAppendableUtf8
import kokoro.internal.io.asClearing
import kotlinx.html.BODY
import kotlinx.html.HEAD
import kotlinx.html.HTML
import kotlinx.html.TagConsumer
import kotlinx.html.emptyMap
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import kotlinx.html.visitTag
import okio.Buffer

// KLUDGE for https://youtrack.jetbrains.com/issue/KT-45505
inline fun HtmlAssetTemplate(
	crossinline body: suspend BODY.(spec: WebAssetSpec) -> Unit,
) = HtmlAssetTemplate({}, body)

inline fun HtmlAssetTemplate(
	crossinline head: suspend HEAD.(spec: WebAssetSpec) -> Unit,
	crossinline body: suspend BODY.(spec: WebAssetSpec) -> Unit,
): HtmlAssetTemplate = object : HtmlAssetTemplate() {

	override suspend fun apply(spec: WebAssetSpec, head: HEAD) {
		super.apply(spec, head)
		head.head(spec)
	}

	override suspend fun apply(spec: WebAssetSpec, body: BODY) {
		body.body(spec)
	}
}

open class HtmlAssetTemplate : WebAssetTemplate {

	override suspend fun apply(request: WebRequest, spec: WebAssetSpec): WebResponse {
		val buffer = Buffer()
		buffer.asAppendableUtf8().apply(spec)
		return WebResponse(
			mimeType = "text/html",
			charset = "utf-8",
			buffer.size, buffer.asClearing(),
		)
	}

	// --

	@Suppress("NOTHING_TO_INLINE")
	suspend inline fun Appendable.apply(spec: WebAssetSpec): Appendable {
		apply(spec, this)
		return this
	}

	open suspend fun apply(spec: WebAssetSpec, out: Appendable) {
		out.appendLine("<!DOCTYPE html>")
		out.appendHTML(prettyPrint = DEBUG).apply(spec)
	}

	@Suppress("NOTHING_TO_INLINE")
	suspend inline fun TagConsumer<*>.apply(spec: WebAssetSpec): TagConsumer<*> {
		apply(spec, this)
		return this
	}

	open suspend fun apply(spec: WebAssetSpec, consumer: TagConsumer<*>) {
		HTML(emptyMap, consumer).visitTag { apply(spec) }
	}

	@Suppress("NOTHING_TO_INLINE")
	suspend inline fun HTML.apply(spec: WebAssetSpec) = apply(spec, this)

	open suspend fun apply(spec: WebAssetSpec, html: HTML) {
		HEAD(emptyMap, html.consumer).visitTag { apply(spec) }
		BODY(emptyMap, html.consumer).visitTag { apply(spec) }
	}

	@Suppress("NOTHING_TO_INLINE")
	suspend inline fun HEAD.apply(spec: WebAssetSpec) = apply(spec, this)

	open suspend fun apply(spec: WebAssetSpec, head: HEAD) {
		head.title(spec.query("title") ?: "&#xFEFF;")
	}

	@Suppress("NOTHING_TO_INLINE")
	suspend inline fun BODY.apply(spec: WebAssetSpec) = apply(spec, this)

	@EmptySuper
	open suspend fun apply(spec: WebAssetSpec, body: BODY) = Unit
}
