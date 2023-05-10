package kokoro.app.ui

import kokoro.app.i18n.Locale

abstract class AlertChoice {

	abstract fun getLabel(locale: Locale): Any

	override fun toString(): String =
		getLabel(Locale.ROOT).toString()
}

inline fun AlertChoice(crossinline lazyLabel: (locale: Locale) -> Any) = object : AlertChoice() {
	override fun getLabel(locale: Locale) = lazyLabel(locale)
}
