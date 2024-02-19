package kokoro.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import kokoro.internal.SPECIAL_USE_DEPRECATION
import java.util.Locale

open class CoreApplication : Application() {

	override fun attachBaseContext(base: Context?) {
		super.attachBaseContext(base)
		@Suppress("DEPRECATION_ERROR")
		init = this
	}

	companion object {
		@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
		@PublishedApi @JvmField internal var init: CoreApplication? = null

		@Suppress("NOTHING_TO_INLINE")
		inline fun getOrNull() = @Suppress("DEPRECATION_ERROR") init

		@Suppress("NOTHING_TO_INLINE")
		inline fun get() = @Suppress("DEPRECATION_ERROR") Singleton.instance
	}

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object Singleton {
		@JvmField val instance = getOrNull() ?: throw IllegalStateException(
			"Core application was not initialized."
		)
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		// Update the JVM default `Locale` manually as it may not automatically
		// be updated for us, at least on some devices -- e.g., in the emulator,
		// it seems to be automatically updated. See also,
		// - https://medium.com/@hectorricardomendez/how-to-get-the-current-locale-in-android-fc12d8be6242
		// - https://stackoverflow.com/a/21844639
		// - https://developer.android.com/guide/topics/resources/app-languages
		//
		val locale = newConfig.locales.get(0)
		if (Locale.getDefault() != locale) {
			Locale.setDefault(locale)
		}
	}
}
