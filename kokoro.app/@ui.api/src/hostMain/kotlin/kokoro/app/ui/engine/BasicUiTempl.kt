package kokoro.app.ui.engine

import androidx.annotation.EmptySuper
import kotlinx.html.BODY
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import kotlinx.html.title

open class BasicUiTempl : BaseUiTempl() {

	companion object {
		operator fun invoke(body: BODY.(spec: UiSpec) -> Unit) = object : BasicUiTempl() {
			override fun BODY.buildHtmlBody(spec: UiSpec) = body(spec)
		}
	}

	@EmptySuper
	protected open fun BODY.buildHtmlBody(spec: UiSpec) = Unit

	final override suspend fun buildHtmlContent(spec: UiSpec, out: Appendable) {
		out.appendLine("<!DOCTYPE html>")
		out.appendHTML().html {
			head {
				title(spec.propOrNull("title") ?: "&#xFEFF;")
			}
			body {
				buildHtmlBody(spec)
			}
		}
	}
}
