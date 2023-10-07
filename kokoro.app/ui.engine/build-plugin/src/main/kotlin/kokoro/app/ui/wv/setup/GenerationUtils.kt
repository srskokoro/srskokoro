package kokoro.app.ui.wv.setup

import kotlin.math.min

internal object GenerationUtils {
	private const val IDENTIFIER_ESCAPE = '\u01BB'

	private val BACKTICKS_NEEDED = HashSet<String>().apply {
		// -- https://kotlinlang.org/docs/keyword-reference.html
		// Hard keywords in Kotlin
		add("as")
		add("break")
		add("class")
		add("continue")
		add("do")
		add("else")
		add("false")
		add("for")
		add("fun")
		add("if")
		add("in")
		add("interface")
		add("is")
		add("null")
		add("object")
		add("package")
		add("return")
		add("super")
		add("this")
		add("throw")
		add("true")
		add("try")
		add("typealias")
		add("typeof")
		add("val")
		add("var")
		add("when")
		add("while")
	}

	private val RESERVED_WORDS = HashSet<String>().apply {
		add("") // Added so that it would get escaped
		addAll(BACKTICKS_NEEDED)

		// -- https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#keywords
		// Reserved words in JS
		add("break")
		add("case")
		add("catch")
		add("class")
		add("const")
		add("continue")
		add("debugger")
		add("default")
		add("delete")
		add("do")
		add("else")
		add("export")
		add("extends")
		add("false")
		add("finally")
		add("for")
		add("function")
		add("if")
		add("import")
		add("in")
		add("instanceof")
		add("new")
		add("null")
		add("return")
		add("super")
		add("switch")
		add("this")
		add("throw")
		add("true")
		add("try")
		add("typeof")
		add("var")
		add("void")
		add("while")
		add("with")
		// Reserved words in JS strict mode
		add("let")
		add("static")
		add("yield")
		// Reserved words in JS module code and async function bodies
		add("await")
		// Future reserved words in JS
		add("enum")
		// Future reserved words in JS strict mode
		add("implements")
		add("interface")
		add("package")
		add("private")
		add("protected")
		add("public")
		// Future reserved words in older JS standards
		add("abstract")
		add("boolean")
		add("byte")
		add("char")
		add("double")
		add("final")
		add("float")
		add("goto")
		add("int")
		add("long")
		add("native")
		add("short")
		add("synchronized")
		add("throws")
		add("transient")
		add("volatile")
		// JS identifiers with special meanings
		add("arguments") // Cannot be used as an identifier in strict mode
		add("as")
		add("async")
		add("eval") // Cannot be used as an identifier in strict mode
		add("from")
		add("get")
		add("of")
		add("set")
	}

	fun appendIdentifier(out: StringBuilder, token: String) {
		if (token in RESERVED_WORDS) {
			out.append(token)
			out.append(IDENTIFIER_ESCAPE)
			out.append(IDENTIFIER_ESCAPE)
			return // Skip code below
		}

		var isAllUnderscore = true

		token[0].let { c ->
			if (c != '_') {
				isAllUnderscore = false
				if (c == IDENTIFIER_ESCAPE || !c.isLetter()) {
					appendIdentifierCharEscaped(out, c)
					return@let // Skip code below
				}
			}
			out.append(c)
		}

		for (i in 1 until token.length) {
			val c = token[i]
			if (c != '_') {
				isAllUnderscore = false
				if (c == IDENTIFIER_ESCAPE || !c.isLetterOrDigit()) {
					appendIdentifierCharEscaped(out, c)
					continue // Skip code below
				}
			}
			out.append(c)
		}

		// Identifiers consisting only of underscores are reserved in Kotlin.
		// Now specifically in that case, escaping is required.
		if (isAllUnderscore) {
			out.append(IDENTIFIER_ESCAPE)
			out.append(IDENTIFIER_ESCAPE)
		}
	}

	fun appendIdentifierStart(out: StringBuilder, prefix: String) {
		val n = prefix.length
		if (n <= 0) {
			out.append(IDENTIFIER_ESCAPE)
			out.append(IDENTIFIER_ESCAPE)
			return // Skip code below
		}

		prefix[0].let { c ->
			if (c != '_' && (c == IDENTIFIER_ESCAPE || !c.isLetter())) {
				appendIdentifierCharEscaped(out, c)
				return@let // Skip code below
			}
			out.append(c)
		}

		for (i in 1 until prefix.length) {
			val c = prefix[i]
			if (c != '_' && (c == IDENTIFIER_ESCAPE || !c.isLetterOrDigit())) {
				appendIdentifierCharEscaped(out, c)
				continue // Skip code below
			}
			out.append(c)
		}
	}

	fun appendIdentifierPartAfterStart(out: StringBuilder, part: String) {
		for (c in part) {
			if (c != '_' && (c == IDENTIFIER_ESCAPE || !c.isLetterOrDigit())) {
				appendIdentifierCharEscaped(out, c)
				continue // Skip code below
			}
			out.append(c)
		}
	}

	private fun appendIdentifierCharEscaped(out: StringBuilder, c: Char) {
		out.append(IDENTIFIER_ESCAPE)
		val hex = c.code.toString(16)
		when (hex.length) {
			1 -> {
				out.append('0')
				out.append(hex[0].uppercaseChar())
			}
			2 -> {
				out.append(hex[0].uppercaseChar())
				out.append(hex[1].uppercaseChar())
			}
			3 -> {
				out.append('0')
				out.append(hex[0].uppercaseChar())
				out.append(IDENTIFIER_ESCAPE)
				out.append(hex[1].uppercaseChar())
				out.append(hex[2].uppercaseChar())
			}
			4 -> {
				out.append(hex[0].uppercaseChar())
				out.append(hex[1].uppercaseChar())
				out.append(IDENTIFIER_ESCAPE)
				out.append(hex[2].uppercaseChar())
				out.append(hex[3].uppercaseChar())
			}
			else -> throw AssertionError()
		}
	}

	fun appendInDqString(out: StringBuilder, value: String) {
		for (c in value) {
			when (c) {
				'"', '$', '\\' -> out.append('\\')
			}
			out.append(c)
		}
	}

	fun appendKtPackageHeader(out: StringBuilder, packageSegments: Array<String>, fromIndex: Int, toIndex: Int): Boolean {
		val n = min(toIndex, packageSegments.size)
		var i = fromIndex
		if (i >= n) return false

		out.append("package ")
		appendIdentifier(out, packageSegments[i])
		while (++i < n) {
			out.append('.')
			appendIdentifier(out, packageSegments[i])
		}
		out.appendLine()

		return true
	}

	fun appendKtPackageHeader(out: StringBuilder, packageSegments: List<String>, fromIndex: Int, toIndex: Int): Boolean {
		if (packageSegments !is RandomAccess) {
			return appendKtPackageHeader_nonRandomAccess(out, packageSegments, fromIndex, toIndex)
		}

		val n = min(toIndex, packageSegments.size)
		var i = fromIndex
		if (i >= n) return false

		out.append("package ")
		appendIdentifier(out, packageSegments[i])
		while (++i < n) {
			out.append('.')
			appendIdentifier(out, packageSegments[i])
		}
		out.appendLine()

		return true
	}

	private fun appendKtPackageHeader_nonRandomAccess(out: StringBuilder, packageSegments: List<String>, fromIndex: Int, toIndex: Int): Boolean {
		val n = min(toIndex, packageSegments.size)
		var i = fromIndex
		if (i >= n) return false

		out.append("package ")
		val itr = packageSegments.listIterator(fromIndex)
		appendIdentifier(out, itr.next())
		while (++i < n) {
			out.append('.')
			appendIdentifier(out, itr.next())
		}
		out.appendLine()

		return true
	}
}
