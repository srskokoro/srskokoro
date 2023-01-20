package srs.kokoro.jcef

import me.friwi.jcefmaven.CefBuildInfo
import me.friwi.jcefmaven.EnumPlatform
import me.friwi.jcefmaven.impl.util.macos.UnquarantineUtil
import org.cef.CefApp
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import javax.inject.Inject

abstract class JcefInstallTask @Inject constructor(private val jcef: JcefExtension) : DefaultTask() {
	init {
		group = jcef.taskGroup
		description = "Installs native binaries provided by JCEF Maven."

		// TODO Make task compatible with configuration cache
		@Suppress("LeakingThis")
		notCompatibleWithConfigurationCache("TODO")
	}

	val outputDir @OutputDirectory get() = jcef.outputDir
	val installDirRel @Input get() = jcef.installDirRel
	val installDir @Internal get() = jcef.installDir

	@TaskAction
	fun run() {
		val outputDirFile = outputDir.get().asFile
		val outputDirPath = outputDirFile.path

		val installDirFile = installDir.get().asFile
		val installDirPath = installDirFile.path

		check(installDirPath.startsWith(outputDirPath) && outputDirPath.length.let {
			it == installDirPath.length || installDirPath[it] == File.separatorChar
		}) { "The install directory should be a subdirectory of the output directory." }

		project.run {
			delete(outputDirFile)
			installJcef(installDirFile, jcef.platform)
		}
	}
}

private const val installLock = "install.lock"

private fun Project.installJcef(installDir: File, platform: EnumPlatform) {
	copy {
		from(tarTree(JavaClassResource(CefApp::class.java, jcefBuildRes)))
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
	if (!File(installDir, installLock).createNewFile()) {
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

private fun Project.requireInstallInfo(installDir: File): CefBuildInfo? {
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
