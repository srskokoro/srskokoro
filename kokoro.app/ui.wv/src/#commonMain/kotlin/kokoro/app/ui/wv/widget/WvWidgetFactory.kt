package kokoro.app.ui.wv.widget

import kokoro.app.ui.wv.WvIdPool

abstract class BaseWvWidgetFactory : SchemaWidgetFactory<WvWidget> {
	internal val widgetIdPool = WvIdPool()

	private var deferredException: Throwable? = null

	fun deferException(ex: Throwable) {
		val prev = deferredException
		if (prev == null) {
			deferredException = ex
		} else {
			prev.addSuppressed(ex)
		}
	}

	fun concludeChanges() {
		// TODO Implementation

		deferredException?.let { ex ->
			deferredException = null
			throw ex
		}
	}
}
