package kokoro.app.ui.wv

internal fun ArgumentsBuilder.conclude() {
	if (out.isNotEmpty()) {
		val lastIndex = out.length - 1
		if (out[lastIndex] == ',') {
			out.setLength(lastIndex)
			return
		}
	}
	throw E_AlreadyConcluded()
}

internal fun ArgumentsBuilder.conclude(closing: Char) {
	if (out.isNotEmpty()) {
		val lastIndex = out.length - 1
		if (out[lastIndex] == ',') {
			out[lastIndex] = closing
			return
		}
	}
	throw E_AlreadyConcluded()
}

internal fun ArgumentsBuilder.conclude(closing: String) {
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
