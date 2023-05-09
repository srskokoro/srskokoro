package kokoro.app.i18n

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
}
