package kokoro.app.ui

import kokoro.app.ui.engine.Ui
import kokoro.app.ui.engine.web.HTTPX_UI_X
import kokoro.app.ui.engine.window.UiWindow
import kokoro.app.ui.engine.window.WvContext

object DummyUi : Ui() {
	override val url = "$HTTPX_UI_X/dummy"
	override val title get() = "Dummy UI"
}

class DummyWindow(context: WvContext) : UiWindow(DummyUi, context)
