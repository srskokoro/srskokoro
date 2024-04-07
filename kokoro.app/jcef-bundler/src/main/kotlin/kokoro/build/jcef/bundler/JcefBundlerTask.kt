package kokoro.build.jcef.bundler

import build.api.file.file
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.file.Deleter
import org.gradle.kotlin.dsl.support.useToRun
import java.io.File
import java.io.IOException
import java.util.jar.JarFile
import javax.inject.Inject

abstract class JcefBundlerTask @Inject constructor(
	@get:Nested val jcef: JcefBundlerExtension,
) : DefaultTask() {

	@get:OutputDirectory
	abstract val outputDir: DirectoryProperty

	// --

	@get:Inject
	protected abstract val del: Deleter

	@get:Inject
	protected abstract val files: FileOperations

	@TaskAction
	open fun execute() {
		val outputDir = outputDir.file
		del.ensureEmptyDirectory(outputDir)

		val tmpDir = temporaryDir
		del.ensureEmptyDirectory(tmpDir)

		val tmpTarFile = File(tmpDir, "jcef.tar.gz")
		JarFile(jcef.nativeDependencyJar.file).useToRun {
			getInputStream(getJarEntry(jcef.nativeDependencyTarResource.get())).useToRun {
				tmpTarFile.outputStream().use {
					transferTo(it)
				}
			}
		}

		files.copy {
			from(files.tarTree(tmpTarFile))
			into(outputDir)
		}.run {
			check(didWork) { "Unexpected: nothing copied." }
		}

		// Marks installation as complete -- note: JCEF Maven expects this.
		File(outputDir, INSTALL_LOCK).also {
			it.delete()
			if (!it.createNewFile())
				throw IOException("Could not create `$INSTALL_LOCK` to complete installation")
		}

		del.deleteRecursively(tmpDir)
	}

	companion object {
		const val INSTALL_LOCK = "install.lock"
	}
}
