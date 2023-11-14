package kokoro.app

import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class MainApplication : CoreApplication() {

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		// Update the JVM default `Locale` manually as it may not automatically
		// be updated for us, at least on some devices -- e.g., in the emulator,
		// it seems to be automatically updated. See also,
		// - https://medium.com/@hectorricardomendez/how-to-get-the-current-locale-in-android-fc12d8be6242
		// - https://stackoverflow.com/a/21844639
		// - https://developer.android.com/guide/topics/resources/app-languages
		//
		val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			newConfig.locales.get(0)
		} else {
			@Suppress("DEPRECATION")
			newConfig.locale
		}
		if (Locale.getDefault() != locale) {
			Locale.setDefault(locale)
		}
	}
}
