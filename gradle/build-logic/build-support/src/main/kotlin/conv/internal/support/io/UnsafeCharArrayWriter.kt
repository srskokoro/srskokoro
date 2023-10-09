package conv.internal.support.io

import java.io.CharArrayWriter

// Inspiration:
// - https://github.com/JetBrains/kotlin/blob/v1.8.0/libraries/stdlib/jvm/src/kotlin/io/FileReadWrite.kt#L98
class UnsafeCharArrayWriter(initialCapacity: Int = 4096) : CharArrayWriter(initialCapacity) {
	val buffer: CharArray get() = buf
	val size get() = count
}
