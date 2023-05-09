package kokoro.app.ui

import kokoro.app.i18n.Locale

actual fun interface AlertChoice {

	fun asString(locale: Locale): String
}
