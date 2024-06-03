package kokoro.app.ui.engine

import androidx.annotation.CallSuper
import kokoro.app.ui.engine.web.HTTPX_RES_X_UI_JS
import kokoro.app.ui.engine.web.HtmlAssetTemplate
import kokoro.app.ui.engine.web.WebAssetSpec
import kotlinx.html.BODY
import kotlinx.html.HEAD
import kotlinx.html.script
import kotlinx.html.unsafe
import kotlin.jvm.JvmField

open class UiTemplate : HtmlAssetTemplate() {

	companion object {
		@JvmField val BASE = UiTemplate()
	}

	override suspend fun apply(spec: WebAssetSpec, head: HEAD) {
		super.apply(spec, head)
		head.script(src = HTTPX_RES_X_UI_JS) {}
	}

	@CallSuper
	override suspend fun apply(spec: WebAssetSpec, body: BODY) {
		val uiFqn = spec.query(UiSpec.PROP_UI)
		if (uiFqn != null) body.script {
			unsafe { +"uiJs.init(uiJs."; +uiFqn; +")" }
		}
	}
}
