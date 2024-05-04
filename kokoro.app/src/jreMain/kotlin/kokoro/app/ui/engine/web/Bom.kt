package kokoro.app.ui.engine.web

import kokoro.internal.SPECIAL_USE_DEPRECATION
import okio.ByteString

object Bom {

	val UTF_8 inline get() = @Suppress("DEPRECATION_ERROR") Bom_UTF_8.value

	val UTF_16BE inline get() = @Suppress("DEPRECATION_ERROR") Bom_UTF_16BE.value
	val UTF_16LE inline get() = @Suppress("DEPRECATION_ERROR") Bom_UTF_16LE.value

	val UTF_32BE inline get() = @Suppress("DEPRECATION_ERROR") Bom_UTF_32BE.value
	val UTF_32LE inline get() = @Suppress("DEPRECATION_ERROR") Bom_UTF_32LE.value

	fun forMediaCharset(charset: String): ByteString? {
		// See,
		// - https://encoding.spec.whatwg.org/commit-snapshots/239008dde6332c07402854a105fc80709f80037e/#concept-encoding-get
		// - https://encoding.spec.whatwg.org/#concept-encoding-get
		return when (charset) {

			"unicode-1-1-utf-8",
			"unicode11utf8",
			"unicode20utf8",
			"utf-8",
			"utf8",
			"x-unicode20utf8",
			-> UTF_8

			"unicodefffe",
			"utf-16be",
			-> UTF_16BE

			"csunicode",
			"iso-10646-ucs-2",
			"ucs-2",
			"unicode",
			"unicodefeff",
			"utf-16",
			"utf-16le",
			-> UTF_16LE

			"utf-32be",
			-> UTF_32BE

			"utf-32le",
			-> UTF_32LE

			"utf-32", // See, https://www.unicode.org/reports/tr19/tr19-9.html

			"ansi_x3.4-1968",
			"ascii",
			"cp1252",
			"cp819",
			"csisolatin1",
			"ibm819",
			"iso-8859-1",
			"iso-ir-100",
			"iso8859-1",
			"iso88591",
			"iso_8859-1",
			"iso_8859-1:1987",
			"l1",
			"latin1",
			"us-ascii",
			"windows-1252",
			"x-cp1252",
			-> ByteString.EMPTY

			else -> null
		}
	}
}

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi
internal object Bom_UTF_8 {
	@JvmField val value = ByteString.of(
		0xEF.toByte(),
		0xBB.toByte(),
		0xBF.toByte(),
	)
}

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi
internal object Bom_UTF_16BE {
	@JvmField val value = ByteString.of(
		0xFE.toByte(),
		0xFF.toByte(),
	)
}

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi
internal object Bom_UTF_16LE {
	@JvmField val value = ByteString.of(
		0xFF.toByte(),
		0xFE.toByte(),
	)
}

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi
internal object Bom_UTF_32BE {
	@JvmField val value = ByteString.of(
		0x00.toByte(),
		0x00.toByte(),
		0xFE.toByte(),
		0xFF.toByte(),
	)
}

@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
@PublishedApi
internal object Bom_UTF_32LE {
	@JvmField val value = ByteString.of(
		0xFF.toByte(),
		0xFE.toByte(),
		0x00.toByte(),
		0x00.toByte(),
	)
}
