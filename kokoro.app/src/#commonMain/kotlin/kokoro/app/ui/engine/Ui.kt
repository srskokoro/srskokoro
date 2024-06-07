package kokoro.app.ui.engine

import kokoro.app.ui.engine.web.HTTPX_RES_X_UI_JS
import kokoro.app.ui.engine.web.HtmlResource
import kotlinx.html.BODY
import kotlinx.html.HEAD
import kotlinx.html.script
import kotlinx.html.unsafe

abstract class Ui : HtmlResource() {

	abstract val url: String

	open val uiFqn: String? get() = this::class.qualifiedName

	override fun feed(head: HEAD) {
		super.feed(head)
		head.script(src = HTTPX_RES_X_UI_JS) {}
	}

	override fun feed(body: BODY) {
		val uiFqn = uiFqn
		if (uiFqn != null) body.script {
			unsafe { +"uiJs.init(uiJs."; +uiFqn; +")" }
		}
	}
}
