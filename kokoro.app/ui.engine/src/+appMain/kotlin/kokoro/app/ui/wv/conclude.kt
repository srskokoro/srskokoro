package kokoro.app.ui.wv

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun conclude(args: ArgumentsBuilder) = conclude(args.out)

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun conclude(args: ArgumentsBuilder, closing: Char) = conclude(args.out, closing = closing)

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun conclude(args: ArgumentsBuilder, closing: String) = conclude(args.out, closing = closing)

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
	// Otherwise, nothing to do.
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
	builder.append(closing)
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
	builder.append(closing)
}
