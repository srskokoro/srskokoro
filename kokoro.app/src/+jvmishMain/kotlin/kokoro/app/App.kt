package kokoro.app

import kokoro.app.i18n.Locale

actual object App {

	@Suppress("NOTHING_TO_INLINE")
	actual inline fun currentLocale(): Locale = Locale.getDefault()
}
