package kokoro.internal.io

import okio.BufferedSink

@Suppress("NOTHING_TO_INLINE")
inline fun BufferedSink.writeUtf8(char: Char) = writeUtf8CodePoint(char.code)
