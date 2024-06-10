package kokoro.internal.io

import okio.BufferedSink

inline fun BufferedSink.writeUtf8(char: Char) = writeUtf8CodePoint(char.code)
