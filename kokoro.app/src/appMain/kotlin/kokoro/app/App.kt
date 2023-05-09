package kokoro.app

import kokoro.app.i18n.Locale

expect object App {

	fun currentLocale(): Locale
}
