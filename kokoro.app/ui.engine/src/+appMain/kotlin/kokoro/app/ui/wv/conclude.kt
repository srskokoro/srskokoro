package kokoro.app.ui.wv

@PublishedApi internal inline fun conclude(args: ArgumentsBuilder) = conclude(args.out)
@PublishedApi internal inline fun conclude(args: ArgumentsBuilder, closing: Char) = conclude(args.out, closing = closing)
@PublishedApi internal inline fun conclude(args: ArgumentsBuilder, closing: String) = conclude(args.out, closing = closing)

// --

@PublishedApi
internal fun conclude(out: StringBuilder) {
	if (out.isNotEmpty()) {
		val lastIndex = out.length - 1
		if (out[lastIndex] == ',') {
			out.setLength(lastIndex)
			return
		}
	}
	throw E_AlreadyConcluded()
}

@PublishedApi
internal fun conclude(out: StringBuilder, closing: Char) {
	if (out.isNotEmpty()) {
		val lastIndex = out.length - 1
		if (out[lastIndex] == ',') {
			out[lastIndex] = closing
			return
		}
	}
	throw E_AlreadyConcluded()
}

@PublishedApi
internal fun conclude(out: StringBuilder, closing: String) {
	if (out.isNotEmpty()) {
		val lastIndex = out.length - 1
		if (out[lastIndex] == ',') {
			out.setRange(lastIndex, lastIndex + 1, closing)
			return
		}
	}
	throw E_AlreadyConcluded()
}

private fun E_AlreadyConcluded() = IllegalStateException("Already concluded")
