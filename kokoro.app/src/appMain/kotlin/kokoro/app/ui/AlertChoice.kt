package kokoro.app.ui

import kokoro.app.i18n.Locale

expect abstract class AlertChoice : `-AlertChoice-common`

abstract class `-AlertChoice-common` {

	abstract fun toString(locale: Locale): Any

	override fun toString(): String =
		toString(Locale.ROOT).toString()
}
