package kokoro.internal.io

import java.io.File
import java.nio.file.Path

typealias NioPath = Path

/**
 * @throws java.nio.file.InvalidPathException
 *
 * @see Path.of
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toNioPath(): Path = NioPath.of(this)

/**
 * @throws java.nio.file.InvalidPathException
 *
 * @see File.toPath
 */
@Suppress("NOTHING_TO_INLINE")
inline fun File.toNioPath(): Path = toPath()
