package kokoro.app

import kokoro.app.ui.DummyWindow
import kokoro.app.ui.engine.window.WvWindowFactory
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.annotation.MainThread

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@MainThread
fun WvWindowFactory.Companion.globalInit() {
	register(from(::DummyWindow))
}
