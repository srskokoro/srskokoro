package kokoro.app.ui

import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div

@JsExport
fun DummyUi() {
	document.body!!.append.div {
		+"Hello World!"
	}
}
