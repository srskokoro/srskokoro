package build.api.support.io

import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@DslMarker
annotation class DirectoryBuilderDsl

@DirectoryBuilderDsl
@JvmInline
value class DirectoryBuilder(@JvmField val dir: File) {

	init {
		dir.initDirs()
	}

	fun dir(child: String) = file(child) {
		initDirs()
	}

	@OptIn(ExperimentalContracts::class)
	inline fun dir(child: String, configure: DirectoryBuilder.() -> Unit): File {
		contract {
			callsInPlace(configure, InvocationKind.EXACTLY_ONCE)
		}
		return buildDir(file(child), configure)
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun clean() = dir.clean()

	// --

	inline val file get() = dir

	@Suppress("NOTHING_TO_INLINE")
	inline fun file(child: String) = File(dir, child)

	@OptIn(ExperimentalContracts::class)
	inline fun file(child: String, configure: File.() -> Unit): File {
		contract {
			callsInPlace(configure, InvocationKind.EXACTLY_ONCE)
		}
		return File(dir, child).apply(configure)
	}
}

@OptIn(ExperimentalContracts::class)
inline fun buildDir(root: File, configure: DirectoryBuilder.() -> Unit): File {
	contract {
		callsInPlace(configure, InvocationKind.EXACTLY_ONCE)
	}
	DirectoryBuilder(root).configure()
	return root
}

@Suppress("NOTHING_TO_INLINE")
inline fun buildDir(root: File) = root.initDirs()

@OptIn(ExperimentalContracts::class)
@JvmName("File buildDir") @JvmSynthetic
inline fun File.buildDir(configure: DirectoryBuilder.() -> Unit): File {
	contract {
		callsInPlace(configure, InvocationKind.EXACTLY_ONCE)
	}
	return buildDir(this, configure)
}

@Suppress("NOTHING_TO_INLINE")
@JvmName("File buildDir") @JvmSynthetic
inline fun File.buildDir() = initDirs()
