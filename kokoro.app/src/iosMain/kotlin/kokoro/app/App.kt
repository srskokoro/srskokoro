package kokoro.app

import kokoro.app.i18n.Locale
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.variantCode

actual object App {

	actual fun currentLocale(): Locale {
		val ns = NSLocale.currentLocale

		val cached: Pair<NSLocale, Locale>? = currentLocale_cached
		if (cached != null) {
			// IDE complains about `==` when type `Any` isn't specified :P
			val cachedNs: Any = cached.first
			if (cachedNs == ns) {
				return cached.second
			}
		}

		// From, https://github.com/comahe-de/i18n4k/blob/v0.5.0/i18n4k-core/src/appleCommonMain/kotlin/de.comahe.i18n4k/Locale.kt
		val locale = Locale(
			language = ns.languageCode,
			country = ns.countryCode.orEmpty(),
			variant = ns.variantCode.orEmpty()
		)
		currentLocale_cached = ns to locale

		return locale
	}
}

private var currentLocale_cached: Pair<NSLocale, Locale>? = null
