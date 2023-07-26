package kokoro.app.ui.wv.widget

import kokoro.app.ui.widget.SchemaWidgetFactory
import kokoro.app.ui.wv.WvIdPool

abstract class BaseWvWidgetFactory : SchemaWidgetFactory<WvWidget> {
	internal val widgetIdPool = WvIdPool()
}
