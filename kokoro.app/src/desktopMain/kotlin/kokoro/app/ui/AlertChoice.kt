package kokoro.app.ui

import kokoro.app.i18n.Locale

actual abstract class AlertChoice : `-AlertChoice-common`()

inline fun AlertChoice(crossinline toString: (locale: Locale) -> Any) = object : AlertChoice() {
	override fun toString(locale: Locale) = toString(locale)
}
