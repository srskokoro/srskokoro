package kokoro.app

import kokoro.app.i18n.Locale
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.variantCode

actual object App {

	actual fun currentLocale(): Locale {
		currentLocale_cached.loop { cached ->
			val ns = NSLocale.currentLocale

			if (cached != null) {
				// IDE complains about `==` when type `Any` isn't specified :P
				val cachedNs: Any = cached.platformLocale
				if (cachedNs == ns) {
					return cached.appLocale
				}
			}

			// From, https://github.com/comahe-de/i18n4k/blob/v0.5.0/i18n4k-core/src/appleCommonMain/kotlin/de.comahe.i18n4k/Locale.kt
			val locale = Locale(
				language = ns.languageCode,
				country = ns.countryCode.orEmpty(),
				variant = ns.variantCode.orEmpty(),
			)
			if (currentLocale_cached.compareAndSet(cached, LocaleCacheEntry(ns, locale))) {
				return locale
			}
		}
	}
}

private class LocaleCacheEntry(
	val platformLocale: NSLocale,
	val appLocale: Locale,
)

private var currentLocale_cached = atomic<LocaleCacheEntry?>(null)
