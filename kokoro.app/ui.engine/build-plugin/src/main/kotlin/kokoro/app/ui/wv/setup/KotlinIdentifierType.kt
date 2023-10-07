package kokoro.app.ui.wv.setup

internal enum class KotlinIdentifierType(
	val isUsableWhenEscaped: Boolean = true,
	val isUsableAsPrefix: Boolean = isUsableWhenEscaped,
	val isUsableAfterStart: Boolean = isUsableWhenEscaped,

	val isUsableAsPackageName: Boolean = isUsableAsPrefix,
	val isUsableAsClassName: Boolean = isUsableAsPrefix,
	val isUsableAsVariableName: Boolean = isUsableAsPrefix,
) {

	HARD_KEYWORD(
		isUsableAsPackageName = false,
		isUsableAsClassName = false,
		isUsableAsVariableName = false,
	),
	SOFT_KEYWORD,
	MODIFIER_KEYWORD,

	USABLE_AS_IS,
	USABLE_AFTER_START(
		isUsableAsPrefix = false,
	),
	NEEDS_ESCAPING(
		isUsableAsPrefix = false,
		isUsableAfterStart = false,
	),
	NEEDS_SPECIAL_INTERVENTION(
		isUsableWhenEscaped = false,
	),

	;

	companion object {
		fun analyze(token: String): KotlinIdentifierType = when (token) {
			// --
			// Generated from, https://kotlinlang.org/docs/keyword-reference.html#hard-keywords
			"as",
			"break",
			"class",
			"continue",
			"do",
			"else",
			"false",
			"for",
			"fun",
			"if",
			"in",
			"interface",
			"is",
			"null",
			"object",
			"package",
			"return",
			"super",
			"this",
			"throw",
			"true",
			"try",
			"typealias",
			"typeof",
			"val",
			"var",
			"when",
			"while",
			-> HARD_KEYWORD
			// --
			// Generated from, https://kotlinlang.org/docs/keyword-reference.html#soft-keywords
			"by",
			"catch",
			"constructor",
			"delegate",
			"dynamic",
			"field",
			"file",
			"finally",
			"get",
			"import",
			"init",
			"param",
			"property",
			"receiver",
			"set",
			"setparam",
			"value",
			"where",
			-> SOFT_KEYWORD
			// --
			// Generated from, https://kotlinlang.org/docs/keyword-reference.html#modifier-keywords
			"abstract",
			"actual",
			"annotation",
			"companion",
			"const",
			"crossinline",
			"data",
			"enum",
			"expect",
			"external",
			"final",
			"infix",
			"inline",
			"inner",
			"internal",
			"lateinit",
			"noinline",
			"open",
			"operator",
			"out",
			"override",
			"private",
			"protected",
			"public",
			"reified",
			"sealed",
			"suspend",
			"tailrec",
			"vararg",
			-> MODIFIER_KEYWORD
			// --
			else -> run(fun(): KotlinIdentifierType {
				val token_n = token.length
				if (token_n > 0) {
					val map = AsciiIdentifierChars.FLAGS
					val map_n = map.size

					var m = token[0].code - AsciiIdentifierChars.FLAGS_offset
					var seenFlags =
						if (m in 0 until map_n) map[m].toInt()
						else AsciiIdentifierChars.FLAGS_fallback
					val initFlags = seenFlags

					for (i in 1 until token_n) {
						m = token[i].code - AsciiIdentifierChars.FLAGS_offset
						seenFlags = seenFlags or
							if (m in 0 until map_n) map[m].toInt()
							else AsciiIdentifierChars.FLAGS_fallback
					}

					if (seenFlags and AsciiIdentifierChars.FLAG_NEEDS_SPECIAL_INTERVENTION == 0) {
						if (seenFlags and AsciiIdentifierChars.FLAG_NEEDS_ESCAPING == 0) {
							if (seenFlags and AsciiIdentifierChars.FLAG_VALID_UNESCAPED != 0) {
								if (initFlags and AsciiIdentifierChars.FLAG_VALID_AT_START != 0) {
									return USABLE_AS_IS
								} else
									return USABLE_AFTER_START
							}
						}
						return NEEDS_ESCAPING
					}
				}
				return NEEDS_SPECIAL_INTERVENTION
			})
		}
	}
}

private object AsciiIdentifierChars {
	const val FLAG_NEEDS_SPECIAL_INTERVENTION = 1 shl 0
	const val FLAG_NEEDS_ESCAPING = 1 shl 1
	const val FLAG_VALID_UNESCAPED = 1 shl 2
	const val FLAG_VALID_AT_START = 1 shl 3

	const val FLAGS_fallback = FLAG_NEEDS_ESCAPING

	const val FLAGS_offset = '0'.code
	val FLAGS = ByteArray(('z'.code + 1) - FLAGS_offset, fun(i) = when (i) {

		in '0'.code..'9'.code,
		-> FLAG_VALID_UNESCAPED.toByte()

		in 'A'.code..'Z'.code,
		in 'a'.code..'z'.code,
		-> (FLAG_VALID_UNESCAPED or FLAG_VALID_AT_START).toByte()

		// Identifiers consisting only of underscores are reserved in Kotlin.
		// Specifically in that case, escaping is required.
		'_'.code,
		-> FLAG_VALID_AT_START.toByte()

		// See,
		// - https://kotlinlang.org/docs/reference/grammar.html#Identifier
		// - https://github.com/JetBrains/kotlin/blob/v1.9.10/compiler/frontend.java/src/org/jetbrains/kotlin/resolve/jvm/checkers/JvmSimpleNameBacktickChecker.kt#L30
		'\r'.code, '\n'.code,
		'`'.code, '.'.code, ';'.code, ':'.code,
		'\\'.code, '/'.code,
		'['.code, ']'.code,
		'<'.code, '>'.code,
		'?'.code, '*'.code, '"'.code, '|'.code, '%'.code, // These characters can cause problems on Windows.
		in '\u0000'.code..'\u0020'.code, '\u007F'.code, // All ASCII control characters
		-> FLAG_NEEDS_SPECIAL_INTERVENTION.toByte()

		else
		-> FLAG_NEEDS_ESCAPING.toByte()
	})
}
