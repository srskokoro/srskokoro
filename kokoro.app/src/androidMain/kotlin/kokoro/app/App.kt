package kokoro.app

import android.os.Build
import kokoro.app.i18n.Locale

actual object App {

	actual fun currentLocale(): Locale {
		// See also,
		// - https://medium.com/@hectorricardomendez/how-to-get-the-current-locale-in-android-fc12d8be6242
		// - https://developer.android.com/guide/topics/resources/app-languages
		val configuration = MainApplication.get().resources.configuration
		return if (Build.VERSION.SDK_INT >= 24) {
			configuration.locales.get(0)
		} else {
			@Suppress("DEPRECATION")
			configuration.locale
		}
	}
}
