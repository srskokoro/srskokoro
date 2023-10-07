package kokoro.app.ui.wv.setup

import java.util.regex.Pattern

internal object KotlinGenerationUtils {

	/**
	 * See, [String literals | Strings | Kotlin Documentation](https://kotlinlang.org/docs/strings.html#string-literals)
	 */
	private val REGEX_SPECIAL_CHARS_IN_ESCAPED_STRING = Pattern.compile("""["$\\]""")

	fun appendInQuotedString(out: StringBuilder, value: CharSequence) {
		val m = REGEX_SPECIAL_CHARS_IN_ESCAPED_STRING.matcher(value)
		while (m.find()) {
			m.appendReplacement(out, """\\$0""")
		}
		m.appendTail(out)
	}

	fun appendPackageHeader(out: StringBuilder, packageSegments: List<String>): Boolean {
		val n = packageSegments.size
		if (n > 0) {
			out.append("package ")
			if (packageSegments is RandomAccess) {
				appendAsPackageSegment(out, packageSegments[0])
				for (i in 1 until n) {
					out.append('.')
					appendAsPackageSegment(out, packageSegments[i])
				}
			} else {
				val itr = packageSegments.iterator()
				appendAsPackageSegment(out, itr.next())
				while (itr.hasNext()) {
					out.append('.')
					appendAsPackageSegment(out, itr.next())
				}
			}
			out.appendLine()
			return true
		}
		return false
	}

	fun appendAsPackageSegment(out: StringBuilder, value: String) {
		val analysis = KotlinIdentifierType.analyze(value)
		if (analysis.isUsableAsPackageName) {
			out.append(value)
		} else {
			appendAsEscapedIdentifier(out, value, analysis)
		}
	}

	fun appendAsEscapedIdentifier(out: StringBuilder, value: String) =
		appendAsEscapedIdentifier(out, value, KotlinIdentifierType.analyze(value))

	fun appendAsEscapedIdentifier(out: StringBuilder, value: String, analysis: KotlinIdentifierType) {
		out.append('`')
		appendInEscapedIdentifier(out, value, analysis)
		out.append('`')
	}

	fun appendInEscapedIdentifier(out: StringBuilder, value: String) =
		appendInEscapedIdentifier(out, value, KotlinIdentifierType.analyze(value))

	fun appendInEscapedIdentifier(out: StringBuilder, value: String, analysis: KotlinIdentifierType) {
		if (analysis.isUsableWhenEscaped) {
			out.append(value)
		} else {
			for (c in value)
				appendInEscapedIdentifier(out, c)
		}
	}

	fun appendInEscapedIdentifier(out: StringBuilder, value: Char) {
		when (value) {
			// See,
			// - https://kotlinlang.org/docs/reference/grammar.html#Identifier
			// - https://github.com/JetBrains/kotlin/blob/v1.9.10/compiler/frontend.java/src/org/jetbrains/kotlin/resolve/jvm/checkers/JvmSimpleNameBacktickChecker.kt#L30
			'\r', '\n',
			'`', '.', ';', ':',
			'\\', '/',
			'[', ']',
			'<', '>',
			'?', '*', '"', '|', '%', // These characters can cause problems on Windows.
			in '\u0000'..'\u0020', '\u007F', // All ASCII control characters
			-> {
				// '%' isn't a reserved character in Windows, but it may cause
				// problems in the command shell. Nonetheless, let's use it as
				// an escape sequence.
				out.append('%')
				out.append(value.code.toString(16))
			}
			else -> out.append(value)
		}
	}
}

@Suppress("RegExpSimplifiable")
private val isSimpleKotlinIdentifier_regex = Regex("""[\w&&\D]\w*+""")

internal fun isSimpleKotlinIdentifier(name: String) = name.matches(isSimpleKotlinIdentifier_regex)

internal fun appendKotlinIdentifier(out: StringBuilder, name: String) {
	if (isSimpleKotlinIdentifier(name)) {
		out.append(name)
	} else {
		out.append('`')
		out.append(name)
		out.append('`')
	}
}
