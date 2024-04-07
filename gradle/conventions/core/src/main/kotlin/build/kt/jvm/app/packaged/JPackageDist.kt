package build.kt.jvm.app.packaged

import build.api.file.file
import build.api.process.ExecArgs
import build.support.Os
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.file.Deleter
import org.gradle.process.ExecOperations
import org.gradle.process.internal.ExecException
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.jar.JarFile
import javax.inject.Inject

abstract class JPackageDist : JPackageBaseTask() {

	/** The expected version of the Java runtime for running the application. */
	@get:Input
	abstract val runtimeVersion: Property<Int>

	@get:Internal
	abstract val appDir: DirectoryProperty

	init {
		@Suppress("LeakingThis")
		// NOTE: While we could've used `@InputDirectory`, that apparently
		// implies `@IgnoreEmptyDirectories`, which we don't want.
		inputs.files(appDir.asFileTree)
			.withPropertyName(::appDir.name)
			.withPathSensitivity(PathSensitivity.RELATIVE)
	}

	/**
	 * The main JAR of the application (containing the main class), specified as
	 * a path relative to the [appDir] path).
	 */
	@get:Input
	abstract val mainJar: Property<String>

	@get:Optional
	@get:Input
	abstract val mainClass: Property<String>

	@get:OutputDirectory
	abstract val outputDir: DirectoryProperty

	// --

	@get:Inject
	protected abstract val del: Deleter

	@get:Inject
	protected abstract val files: FileOperations

	@get:Inject
	protected abstract val exec: ExecOperations

	/**
	 * Provides a way to inspect the arguments used after execution; exposed
	 * only for debugging purposes.
	 *
	 * @see ExecArgs.toString
	 */
	@get:Internal
	lateinit var jdepsExecArgs: ExecArgs

	@TaskAction
	open fun execute() {
		val outputDir = outputDir.file
		del.ensureEmptyDirectory(outputDir)

		// --

		initJPackageExecArgs()

		val tmpDestDir = temporaryDir
		del.ensureEmptyDirectory(tmpDestDir)

		val appDir = appDir.file
		val mainJar = mainJar.get()

		jpackageExecArgs.apply {
			args("-t", "app-image")
			args("-d", tmpDestDir.path)
			args("-i", appDir.path)
			args("--main-jar", mainJar)
			mainClass.orNull?.let {
				args("--main-class", it)
			}
		}

		jdepsExecArgs = ExecArgs {
			val mainJarFile = File(appDir, mainJar)
			if (JarFile(mainJarFile, false).use { it.isMultiRelease }) {
				args("--multi-release", runtimeVersion.get().str)
			}
			args("--print-module-deps")
			args("--ignore-missing-deps")
			args(mainJarFile.path)
		}

		val jdepsOutput = ByteArrayOutputStream()
		val jdepsExecResult = exec.exec {
			standardOutput = jdepsOutput
			executable = jdepsPath()
			args(jdepsExecArgs)
			isIgnoreExitValue = true // We'll manually throw below
		}
		val jdepsOutputStr = jdepsOutput.toString()
		try {
			jdepsExecResult.rethrowFailure().assertNormalExitValue()
		} catch (ex: ExecException) {
			throw ExecException(StringBuilder().apply {
				val m = ex.message
				if (!m.isNullOrEmpty()) {
					m.lineSequence()
						.forEach { appendLine(it) }
					appendLine()
				}
				jdepsOutputStr.lineSequence()
					.forEach { appendLine(it) }
			}.trimEnd() as String, ex)
		}

		jdepsOutputStr.trim().let {
			jpackageExecArgs.args("--add-modules", it)
		}

		val res = spec.jpackageResources.file
		val icon: File

		when (Os.current) {
			Os.WINDOWS -> {
				icon = File(res, RES_ICON_WIN_FILE)
			}
			Os.MACOS -> {
				icon = File(res, RES_ICON_MAC_FILE)
			}
			Os.LINUX -> {
				icon = File(res, RES_ICON_LINUX_FILE)
			}
		}

		if (icon.isFile) {
			jpackageExecArgs.args("--icon", icon.path)
		}

		jpackageExecArgs.apply {
			val spec = spec
			spec.bundleAdditions.asFileTree.matching {
				include("*")
			}.visit {
				val path = file.absolutePath
				check(path.none { it == ',' || it == '\'' || it == '"' }) {
					"Option `--app-content` does not support paths containing commas (i.e., ',') or quotes (i.e., `\'` or `\"`)\n" +
						"- Offending path: $path"
					// See also, https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html#options-for-creating-the-application-image
				}
				args("--app-content", path)
			}

			spec.jvmArgs.get().forEach {
				args("--java-options", it)
			}
		}

		val result = exec.exec {
			executable = jpackagePath()
			args(jpackageExecArgs)
			isIgnoreExitValue = true
		}.runCatching {
			rethrowFailure()
			assertNormalExitValue()
		}

		// --

		val isSuccess = result.isSuccess
		for (x in File(tmpDestDir, jpackageExecArgs_name).listFiles()!!) {
			// Necessary since `jpackage` seems to output executable files as
			// read-only, which may prevent the JVM from deleting them.
			x.setWritable(true) // Allow the JVM (and Gradle) to delete it.

			if (isSuccess) File(outputDir, x.name).let { d ->
				if (!x.renameTo(d))
					throw FileSystemException(x, d, "Failed to move file.")
			}
		}
		result.onFailure { throw it }

		del.deleteRecursively(tmpDestDir)
	}
}
