package kokoro.app.ui

import kokoro.app.i18n.Locale

abstract class AlertChoice {

	abstract fun getButton(locale: Locale): Any

	abstract fun getLabel(locale: Locale): Any

	override fun toString(): String =
		getLabel(Locale.ROOT).toString()
}

inline fun AlertChoice(
	crossinline lazyButton: AlertChoice.(Locale) -> Any = { getLabel(it) },
	crossinline lazyLabel: AlertChoice.(Locale) -> Any,
) = object : AlertChoice() {
	override fun getButton(locale: Locale) = lazyButton(locale)
	override fun getLabel(locale: Locale) = lazyLabel(locale)
}
