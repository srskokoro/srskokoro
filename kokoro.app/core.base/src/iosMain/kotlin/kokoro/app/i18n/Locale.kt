package kokoro.app.i18n

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.variantCode

actual data class Locale actual constructor(
	actual val language: String,
	actual val country: String,
	actual val variant: String,
) {

	actual constructor(
		language: String,
		country: String,
	) : this(
		language = language,
		country = country,
		variant = "",
	)

	actual constructor(
		language: String,
	) : this(
		language = language,
		country = "",
		variant = "",
	)

	// TODO Switch to Kotlin `static` instead, once available -- https://youtrack.jetbrains.com/issue/KT-11968
	actual companion object {
		actual val ROOT = Locale("", "", "")
	}
}

private class LocaleCacheEntry(
	val platformLocale: NSLocale,
	val commonLocale: Locale,
)

private var currentLocale_cache = atomic<LocaleCacheEntry?>(null)

actual fun currentLocale(): Locale = currentLocale_cache.loop { cached ->
	val ns = NSLocale.currentLocale

	if (cached != null) {
		// IDE complains about `==` when type `Any` isn't specified :P
		val cachedNs: Any = cached.platformLocale
		if (cachedNs == ns) {
			return cached.commonLocale
		}
	}

	// From, https://github.com/comahe-de/i18n4k/blob/v0.5.0/i18n4k-core/src/appleCommonMain/kotlin/de.comahe.i18n4k/Locale.kt
	val locale = Locale(
		language = ns.languageCode,
		country = ns.countryCode.orEmpty(),
		variant = ns.variantCode.orEmpty(),
	)
	if (currentLocale_cache.compareAndSet(cached, LocaleCacheEntry(ns, locale))) {
		return locale
	}
}
