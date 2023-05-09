package kokoro.app.ui

import kokoro.app.i18n.Locale

expect abstract class AlertChoice : `-AlertChoice-common`

@Suppress("ClassName")
abstract class `-AlertChoice-common` {

	abstract fun toString(locale: Locale): String

	override fun toString(): String = toString(Locale.ROOT)
}
