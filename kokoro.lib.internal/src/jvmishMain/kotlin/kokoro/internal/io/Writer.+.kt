package kokoro.internal.io

import java.io.Writer

@Suppress("NOTHING_TO_INLINE")
inline fun Writer.writeln() = write(System.lineSeparator())
