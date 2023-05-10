package kokoro.app.ui

import kokoro.app.App
import kokoro.app.i18n.Locale

expect class AlertContext {
	val locale: Locale
}

inline val AlertContext?.locale get() = this?.locale ?: Locale.ROOT

inline val AlertContext?.localeOrCurrent get() = this?.locale ?: App.currentLocale()
