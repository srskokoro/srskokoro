package kokoro.app.ui.engine.web

abstract class BaseWebRequest : WebRequest {

	override fun toString() = buildString {
		append("WebRequest {")

		appendLine()
		append('\t'); append(method)
		append(' '); append(url)

		headers().entries.forEach { (name, value) ->
			appendLine()
			append('\t'); append(name)
			append(": "); append(value)
		}

		appendLine()
		append("}")
	}
}
