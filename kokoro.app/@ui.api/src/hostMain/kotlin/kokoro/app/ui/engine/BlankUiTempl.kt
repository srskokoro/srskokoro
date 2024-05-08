package kokoro.app.ui.engine

import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import kotlinx.html.title

data object BlankUiTempl : BaseUiTempl() {
	override suspend fun buildHtmlContent(spec: UiSpec, out: Appendable) {
		out.appendLine("<!DOCTYPE html>")
		out.appendHTML().html {
			head {
				title(spec.propOrNull("title") ?: "&#xFEFF;")
			}
			body {
				// Blank
			}
		}
	}
}
