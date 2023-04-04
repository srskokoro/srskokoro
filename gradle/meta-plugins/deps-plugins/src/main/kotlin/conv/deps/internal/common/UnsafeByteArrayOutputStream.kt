package conv.deps.internal.common

import java.io.ByteArrayOutputStream

// Inspiration:
// - https://github.com/JetBrains/kotlin/blob/v1.8.0/libraries/stdlib/jvm/src/kotlin/io/FileReadWrite.kt#L98
internal class UnsafeByteArrayOutputStream(size: Int = 4096) : ByteArrayOutputStream(size) {
	val buffer: ByteArray get() = buf
	val size get() = count
}
