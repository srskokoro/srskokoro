package kokoro.app.i18n

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
