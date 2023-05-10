package kokoro.app.ui

import kokoro.app.App
import kokoro.app.i18n.Locale

actual class AlertContext(
	actual val locale: Locale = App.currentLocale(),
)
