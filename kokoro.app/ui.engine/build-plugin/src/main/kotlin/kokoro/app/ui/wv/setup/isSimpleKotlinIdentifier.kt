package kokoro.app.ui.wv.setup

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
