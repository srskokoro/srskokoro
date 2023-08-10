package kokoro.internal.io

import java.io.File
import java.nio.file.Path

typealias NioPath = Path

@Suppress("NOTHING_TO_INLINE")
inline fun String.toNioPath(): Path = NioPath.of(this)

@Suppress("NOTHING_TO_INLINE")
inline fun File.toNioPath(): Path = toPath()
