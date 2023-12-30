package build.support.io

import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@DslMarker
annotation class DirectoryBuilderDsl

@DirectoryBuilderDsl
@JvmInline
value class DirectoryBuilder(@JvmField val file: File) {

	init {
		file.initDirs()
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun file() = file

	@Suppress("NOTHING_TO_INLINE")
	inline fun file(child: String) = File(file, child)

	@OptIn(ExperimentalContracts::class)
	inline fun file(child: String, configure: File.() -> Unit): File {
		contract {
			callsInPlace(configure, InvocationKind.EXACTLY_ONCE)
		}
		return File(file, child).apply(configure)
	}

	// --

	@Suppress("NOTHING_TO_INLINE")
	inline fun dir() = this

	fun dir(child: String) = DirectoryBuilder(file(child))

	@OptIn(ExperimentalContracts::class)
	inline fun dir(child: String, configure: DirectoryBuilder.() -> Unit): DirectoryBuilder {
		contract {
			callsInPlace(configure, InvocationKind.EXACTLY_ONCE)
		}
		return dir(child).apply(configure)
	}

	@OptIn(ExperimentalContracts::class)
	inline fun dir(configure: DirectoryBuilder.() -> Unit): DirectoryBuilder {
		contract {
			callsInPlace(configure, InvocationKind.EXACTLY_ONCE)
		}
		return apply(configure)
	}

	// --

	@Suppress("NOTHING_TO_INLINE")
	inline fun clean() = file.clean()
}

@Suppress("NOTHING_TO_INLINE")
inline fun buildDir(root: File) = DirectoryBuilder(root)

@OptIn(ExperimentalContracts::class)
inline fun buildDir(root: File, configure: DirectoryBuilder.() -> Unit): DirectoryBuilder {
	contract {
		callsInPlace(configure, InvocationKind.EXACTLY_ONCE)
	}
	return DirectoryBuilder(root).apply(configure)
}

@Suppress("NOTHING_TO_INLINE")
@JvmName("File buildDir") @JvmSynthetic
inline fun File.buildDir() = buildDir(this)

@OptIn(ExperimentalContracts::class)
@JvmName("File buildDir") @JvmSynthetic
inline fun File.buildDir(configure: DirectoryBuilder.() -> Unit): DirectoryBuilder {
	contract {
		callsInPlace(configure, InvocationKind.EXACTLY_ONCE)
	}
	return buildDir(this, configure)
}
