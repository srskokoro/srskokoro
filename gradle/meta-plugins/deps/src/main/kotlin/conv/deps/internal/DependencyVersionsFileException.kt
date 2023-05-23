package conv.deps.internal

import org.gradle.api.GradleException
import org.gradle.api.file.FileSystemLocation
import java.io.File
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("MemberVisibilityCanBePrivate")
open class DependencyVersionsFileException(val path: String, cause: Throwable?) : GradleException(
	"""
	Exception related to dependency versions file: $path

	Cause: $cause
	""".trimIndent(),

	cause
) {
	constructor(path: FileSystemLocation, cause: Throwable?) : this(path.asFile, cause)

	constructor(path: Path, cause: Throwable?) : this(path.toFile(), cause)

	constructor(path: File, cause: Throwable?) : this(
		try {
			path.canonicalPath to null
		} catch (ex: Throwable) {
			path.path to ex
		}, cause
	)

	private constructor(
		pathWithSuppressedException: Pair<String, Throwable?>, cause: Throwable?,
	) : this(pathWithSuppressedException.first, cause) {
		pathWithSuppressedException.second?.let {
			addSuppressed(it)
		}
	}

	companion object {
		internal fun wrapJudiciously(path: FileSystemLocation, cause: Throwable?) =
			if (shouldNotWrap(cause)) cause else DependencyVersionsFileException(path, cause)

		internal fun wrapJudiciously(path: Path, cause: Throwable?) =
			if (shouldNotWrap(cause)) cause else DependencyVersionsFileException(path, cause)

		internal fun wrapJudiciously(path: File, cause: Throwable?) =
			if (shouldNotWrap(cause)) cause else DependencyVersionsFileException(path, cause)

		internal fun wrapJudiciously(path: String, cause: Throwable?): Throwable =
			if (shouldNotWrap(cause)) cause else DependencyVersionsFileException(path, cause)

		@Suppress("NOTHING_TO_INLINE")
		@OptIn(ExperimentalContracts::class)
		private inline fun shouldNotWrap(cause: Throwable?): Boolean {
			contract { returns(true) implies (cause is Error) }
			return cause is Error && cause !is AssertionError
		}
	}
}
