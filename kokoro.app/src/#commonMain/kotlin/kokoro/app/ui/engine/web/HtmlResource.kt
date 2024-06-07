package kokoro.app.ui.engine.web

import androidx.annotation.EmptySuper
import kokoro.internal.DEBUG
import kokoro.internal.io.asAppendableUtf8
import kokoro.internal.io.asClearing
import kotlinx.html.BODY
import kotlinx.html.HEAD
import kotlinx.html.HTML
import kotlinx.html.TITLE
import kotlinx.html.TagConsumer
import kotlinx.html.emptyMap
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import kotlinx.html.visitTag
import okio.Buffer

open class HtmlResource : WebResource {

	override suspend fun apply(request: WebRequest): WebResponse {
		val buffer = Buffer()
		feed(buffer.asAppendableUtf8())
		return WebResponse(
			mimeType = "text/html",
			charset = "utf-8",
			buffer.size, buffer.asClearing(),
		)
	}

	// --

	open fun feed(out: Appendable) {
		val consumer = out
			.appendLine("<!DOCTYPE html>")
			.appendHTML(prettyPrint = DEBUG)
		feed(consumer)
	}

	open fun feed(consumer: TagConsumer<*>): Unit =
		HTML(emptyMap, consumer).visitTag { feed(this) }

	open fun feed(html: HTML) {
		HEAD(emptyMap, html.consumer).visitTag { feed(this) }
		BODY(emptyMap, html.consumer).visitTag { feed(this) }
	}

	open fun feed(head: HEAD) {
		val title = title
		head.title(fun(tag: TITLE) {
			tag.text(title)
		})
	}

	@EmptySuper
	open fun feed(body: BODY) = Unit

	// --

	open val title: String get() = "\uFEFF"
}
