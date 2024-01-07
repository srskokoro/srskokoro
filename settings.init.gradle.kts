import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.*
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

extra["autoGradleProperties"] = fun(rootProject: String): String {
	val settingsDir = settings.settingsDir
	val targetDir = File(settingsDir, rootProject)

	/**
	 * @see build.api.provider.RunUntrackedHelper
	 */
	RunUntrackedHelper.runAction(providers) {
		/**
		 * @see build.support.io.transformFileAtomic
		 */
		File(settingsDir, "gradle.properties").let { source ->
			val target = File(targetDir, "gradle.properties")
			if (!source.isFile) {
				target.delete()
				return@let
			}
			transformFileAtomic(source, target) {
				val props = Properties().apply { load(source.inputStream()) }
				props.store(Channels.newOutputStream(it), " Auto-generated file. DO NOT EDIT!")
			}
		}
	}

	return rootProject
}

// --

/**
 * Borrowed with minimal changes from [build.support.io.transformFileAtomic]
 */
inline fun transformFileAtomic(
	source: File,
	destination: File,
	// NOTE: Using `crossinline` here prevents non-local returns.
	crossinline generator: (FileChannel) -> Unit,
): Boolean {
	var outputModMs = source.lastModified()

	// NOTE:
	// - `lastModified()` is `0L` for nonexistent files.
	// - We don't care if the source file doesn't really exist.
	// - We manually timestamp the destination file to have the same
	// modification time as the source file at the time of generation.
	// - If the timestamp is zero, then that should be interpreted as a request
	// for forced (re)generation, even if the destination file already exists.
	if (destination.lastModified() == outputModMs && outputModMs != 0L) {
		return false // The destination file is likely up-to-date!
	}
	// Otherwise, the destination file likely needs (re)generation!

	// --

	// NOTE: The following handles an edge case where the source file gets
	// modified concurrently yet still have the same timestamp as what we got,
	// simply because the wall-clock time is the same as (or less than) the
	// source file's current timestamp.
	if (System.currentTimeMillis() <= outputModMs) {
		// At this point, the source file might have been modified while we're
		// already running (or the source file's timestamp may have been
		// maliciously set to the future). Anyway, force regeneration of the
		// destination file after the current (re)generation.
		outputModMs = 0L
	}

	// --

	val tmp = transformFileAtomic_initTmp(destination)
	try {
		transformFileAtomic_initFc(tmp).use {
			generator(it)
		}
		transformFileAtomic_finish(outputModMs, tmp, destination)
	} catch (ex: Throwable) {
		throw transformFileAtomic_error(destination, tmp, ex)
	}

	return true
}

// --

@PublishedApi
internal fun transformFileAtomic_initTmp(destination: File): File {
	val tmp = File("$destination.tmp")
	if (!tmp.delete()) tmp.parentFile.mkdirs()

	return tmp
}

@PublishedApi
internal fun transformFileAtomic_initFc(tmp: File): FileChannel {
	// Needs `SYNC` here since otherwise, file writes and metadata (especially,
	// modification time) can be delayed by the OS (even on properly closed
	// streams) and we have to do a rename/move operation later to atomically
	// publish our changes.
	return FileChannel.open(tmp.toPath(), SYNC, CREATE, TRUNCATE_EXISTING, WRITE)
}

@PublishedApi
internal fun transformFileAtomic_finish(outputModMs: Long, tmp: File, destination: File) {
	// Set up modification time manually for our custom up-to-date check
	tmp.setLastModified(outputModMs)

	// Atomically publish our changes via a rename/move operation
	Files.move(tmp.toPath(), destination.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
	// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`
}

@PublishedApi
internal fun transformFileAtomic_error(destination: File, tmp: File, cause: Throwable): Throwable {
	try {
		tmp.delete()
	} catch (ex: Throwable) {
		cause.addSuppressed(ex)
	}
	return if (cause is Error) cause
	else FileSystemException(destination)
		.apply { initCause(cause) }
}

// --

/**
 * Borrowed with minimal changes from [build.api.provider.RunUntrackedHelper]
 */
internal abstract class RunUntrackedHelper : ValueSource<Boolean, RunUntrackedHelper.Parameters> {

	interface Parameters : ValueSourceParameters {
		val ticket: Property<Long>
	}

	companion object {
		private val nextTicket = AtomicLong()
		private val actions = ConcurrentHashMap<Long, () -> Unit>()

		fun runAction(providers: ProviderFactory, action: () -> Unit) {
			val ticket = nextTicket.getAndIncrement()
			if (actions.putIfAbsent(ticket, action) != null) {
				throw Error("Maximum number of tickets reached!")
			}
			providers.of(RunUntrackedHelper::class.java) {
				parameters.ticket.set(ticket)
			}.get()
		}
	}

	override fun obtain(): Boolean {
		val ticket = parameters.ticket.get()
		actions.remove(ticket)?.invoke()
		return false
	}
}
