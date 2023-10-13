package kokoro.app.ui.wv

@PublishedApi internal inline fun conclude(args: ArgumentsBuilder) = conclude(args.out)
@PublishedApi internal inline fun conclude(args: ArgumentsBuilder, closing: Char) = conclude(args.out, closing = closing)
@PublishedApi internal inline fun conclude(args: ArgumentsBuilder, closing: String) = conclude(args.out, closing = closing)

// --

@PublishedApi
internal fun conclude(builder: StringBuilder) {
	if (builder.isNotEmpty()) {
		val lastIndex = builder.length - 1
		if (builder[lastIndex] == ',') {
			builder.setLength(lastIndex)
			return
		}
	}
	throw E_AlreadyConcluded()
}

@PublishedApi
internal fun conclude(builder: StringBuilder, closing: Char) {
	if (builder.isNotEmpty()) {
		val lastIndex = builder.length - 1
		if (builder[lastIndex] == ',') {
			builder[lastIndex] = closing
			return
		}
	}
	throw E_AlreadyConcluded()
}

@PublishedApi
internal fun conclude(builder: StringBuilder, closing: String) {
	if (builder.isNotEmpty()) {
		val lastIndex = builder.length - 1
		if (builder[lastIndex] == ',') {
			builder.setRange(lastIndex, lastIndex + 1, closing)
			return
		}
	}
	throw E_AlreadyConcluded()
}

private fun E_AlreadyConcluded() = IllegalStateException("Already concluded")
