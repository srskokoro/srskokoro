package kokoro.app.i18n

import kotlin.jvm.JvmField

/**
 * @see currentLocale
 */
expect class Locale(
	language: String,
	country: String,
	variant: String,
) {
	val language: String
	val country: String
	val variant: String

	constructor(
		language: String,
		country: String,
	)

	constructor(
		language: String,
	)

	// TODO Switch to Kotlin `static` instead, once available -- https://youtrack.jetbrains.com/issue/KT-11968
	companion object {
		/**
		 * @see currentLocale
		 */
		@JvmField val ROOT: Locale
	}
}

expect fun currentLocale(): Locale
