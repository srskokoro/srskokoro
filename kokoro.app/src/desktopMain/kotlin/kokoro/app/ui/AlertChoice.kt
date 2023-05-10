package kokoro.app.ui

import kokoro.app.i18n.Locale

actual abstract class AlertChoice : `-AlertChoice-common`()

inline fun AlertChoice(crossinline lazyLabel: (locale: Locale) -> Any) = object : AlertChoice() {
	override fun getLabel(locale: Locale) = lazyLabel(locale)
}
