package kokoro.app.ui

import kokoro.app.ui.engine.web.HTTPX
import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div

@JsExport
fun DummyUi() {
	document.body!!.append.div {
		+"Hello World! @ $HTTPX"
	}
}
