package kokoro.app.ui

import kokoro.app.i18n.Locale

actual fun interface AlertChoice {

	fun toString(locale: Locale): String
}
