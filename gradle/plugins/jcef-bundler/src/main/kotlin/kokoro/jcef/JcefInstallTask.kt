package kokoro.jcef

import me.friwi.jcefmaven.CefBuildInfo
import me.friwi.jcefmaven.impl.util.macos.UnquarantineUtil
import org.cef.CefApp
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.resources.MissingResourceException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

abstract class JcefInstallTask @Inject constructor() : DefaultTask() {

	val platform @Internal get() = jcefBuildPlatform

	@get:OutputDirectory
	val outputDir: DirectoryProperty = project.objects.directoryProperty()
		.convention(project.layout.buildDirectory.dir(project.provider { "generated/$name" }))

	@get:Inject internal abstract val fsOps: FileSystemOperations
	@get:Inject internal abstract val archiveOps: ArchiveOperations

	@TaskAction
	fun run() {
		val outputDirFile = outputDir.get().asFile
		fsOps.delete { delete(outputDirFile) }
		installJcef(outputDirFile)
	}
}

private const val installLock = "install.lock"

private fun JcefInstallTask.installJcef(outputDir: File) {
	if (!outputDir.mkdirs()) throw IOException("Directory creation failed: $outputDir")

	val tmpTarFile = File(outputDir, "jcef.tar.gz")
	(CefApp::class.java.classLoader.getResourceAsStream(jcefBuildRes)
		?: throw MissingResourceException("Expected classpath resource not found: $jcefBuildRes")).use { input ->
		FileOutputStream(tmpTarFile).use { output -> input.transferTo(output) }
	}

	val installDir = File(outputDir, "jcef")
	fsOps.copy {
		from(archiveOps.tarTree(tmpTarFile))
		into(installDir)
	}.run {
		check(didWork) { "Unexpected: nothing copied." }
	}

	tmpTarFile.delete()

	if (platform.os.isMacOSX) {
		// Remove quarantine on macOS
		// TODO Not sure if this is actually needed. Perhaps quarantine is never set. Need a Mac to confirm.
		UnquarantineUtil.unquarantine(installDir)
	}

	// Marks installation as complete -- note: JCEF Maven expects this.
	File(installDir, installLock).also {
		it.delete()
		if (!it.createNewFile())
			throw IOException("Could not create `$installLock` to complete installation")
	}

	// Verifies that the installed version is correct
	requireInstallInfo(installDir)?.let {
		error(
			"""
			Invalid installation. Please ensure proper configuration.
			[Required gradle prop]: $jcefBuildTagProp=${it.releaseTag}
			 [Current gradle prop]: $jcefBuildTagProp=$jcefBuildTag

			Ideally, set instead the following gradle properties accordingly:
			  $jcefBuildJcefCommitProp
			  $jcefBuildCefVersionProp

			Preferably in, $pluginGradlePropsPath
			""".trimIndent()
		)
	}
}

private fun JcefInstallTask.requireInstallInfo(installDir: File): CefBuildInfo? {
	fun getRequired() = CefBuildInfo.fromClasspath()
	run {
		if (!File(installDir, installLock).exists()) {
			return@run
		}
		val installInfo = File(installDir, "build_meta.json")
		if (!installInfo.exists()) {
			return@run
		}
		val installed = try {
			CefBuildInfo.fromFile(installInfo)
		} catch (e: IOException) {
			logger.warn("Error while parsing installation info from output.", e)
			return@run
		}
		if (installed.platform != platform.identifier) {
			return@run
		}
		return getRequired().takeIf { it.releaseTag != installed.releaseTag }
	}
	return getRequired()
}
