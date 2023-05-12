package kokoro.app

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import java.util.Locale
import kokoro.app.MainApplication.Singleton.instance as mainApplication

class MainApplication : Application() {
	init {
		instance = this
	}

	companion object {
		private var instance: MainApplication? = null

		fun getOrNull(): MainApplication? = instance

		fun get(): MainApplication = mainApplication
	}

	private object Singleton {
		@JvmField val instance: MainApplication =
			getOrNull() ?: throw UninitializedPropertyAccessException(
				"Main application was not initialized."
			)
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		// Update the JVM default `Locale` manually as it may not automatically
		// be done for us, at least for some devices -- e.g., in the emulator,
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
