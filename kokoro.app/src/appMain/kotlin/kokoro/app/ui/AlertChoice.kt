package kokoro.app.ui

import kokoro.app.i18n.Locale

abstract class AlertChoice {

	abstract fun getButton(locale: Locale): Any

	abstract fun getText(locale: Locale): Any

	override fun toString(): String =
		getText(Locale.ROOT).toString()
}

inline fun AlertChoice(
	crossinline lazyButton: AlertChoice.(Locale) -> Any = { getText(it) },
	crossinline lazyText: AlertChoice.(Locale) -> Any,
) = object : AlertChoice() {
	override fun getButton(locale: Locale) = lazyButton(locale)
	override fun getText(locale: Locale) = lazyText(locale)
}

inline fun AlertChoice2(
	crossinline lazyButton: AlertChoice.(text: Any, Locale) -> Any,
	crossinline lazyText: AlertChoice.(Locale) -> Any,
) = object : AlertChoice() {
	override fun getButton(locale: Locale) = lazyButton(getText(locale), locale)
	override fun getText(locale: Locale) = lazyText(locale)
}
