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
}
