package srs.kokoro.jcef

import me.friwi.jcefmaven.CefBuildInfo
import me.friwi.jcefmaven.EnumPlatform
import me.friwi.jcefmaven.impl.util.macos.UnquarantineUtil
import org.cef.CefApp
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import javax.inject.Inject

abstract class JcefInstallTask @Inject constructor(jcef: JcefExtension) : DefaultTask() {
	private val config: JcefConfig = jcef.config

	init {
		group = config.taskGroup
		description = "Installs native binaries provided by JCEF Maven."
	}

	val outputDir: Provider<Directory> @OutputDirectory get() = config.outputDir

	@get:Inject internal abstract val fsOps: FileSystemOperations
	@get:Inject internal abstract val archiveOps: ArchiveOperations

	@TaskAction
	fun run() {
		val outputDirFile = outputDir.get().asFile
		fsOps.delete { delete(outputDirFile) }
		installJcef(outputDirFile, config.platform)
	}
}

private const val installLock = "install.lock"

private fun JcefInstallTask.installJcef(installDir: File, platform: EnumPlatform) {
	fsOps.copy {
		from(archiveOps.tarTree(JavaClassResource(CefApp::class.java, jcefBuildRes)))
		into(installDir)
	}.run {
		check(didWork) { "Unexpected: nothing copied." }
	}
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
		if (installed.platform != EnumPlatform.getCurrentPlatform().identifier) {
			return@run
		}
		return getRequired().takeIf { it.releaseTag != installed.releaseTag }
	}
	return getRequired()
}
