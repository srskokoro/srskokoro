package srs.kokoro.jcef

import me.friwi.jcefmaven.CefBuildInfo
import me.friwi.jcefmaven.EnumPlatform
import me.friwi.jcefmaven.impl.util.macos.UnquarantineUtil
import org.cef.CefApp
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import javax.inject.Inject

abstract class JcefInstallTask @Inject constructor(private val jcef: JcefExtension) : DefaultTask() {
	init {
		group = "jcef"
		description = "Installs native binaries provided by JCEF Maven."
	}

	val outputDir @OutputDirectory get() = jcef.outputDir
	fun outputDir(path: Any) = jcef.outputDir(path)
	fun outputDir(pathProvider: () -> Any) = jcef.outputDir(pathProvider)

	@TaskAction
	fun run() {
		project.installJcef(outputDir.asFile.get(), jcef.platform)
	}
}

private const val installLock = "install.lock"

private fun Project.installJcef(outputDir: File, platform: EnumPlatform) {
	delete(outputDir)
	copy {
		from(tarTree(JavaClassResource(CefApp::class.java, jcefBuildRes)))
		into(outputDir)
	}.run {
		check(didWork) { "Unexpected: nothing copied." }
	}
	if (platform.os.isMacOSX) {
		// Remove quarantine on macOS
		// TODO Not sure if this is actually needed. Perhaps quarantine is never set. Need a Mac to confirm.
		UnquarantineUtil.unquarantine(outputDir)
	}
	// Marks installation as complete -- note: JCEF Maven expects this.
	if (!File(outputDir, installLock).createNewFile()) {
		throw IOException("Could not create `$installLock` to complete installation")
	}
	// Verifies that the installed version is correct
	requireInstallInfo(outputDir)?.let {
		error(
			"""
			Invalid installation. Please ensure proper configuration.
			[Required gradle prop]: $jcefBuildTagProp=${it.releaseTag}
			 [Current gradle prop]: $jcefBuildTagProp=$jcefBuildTag
			""".trimIndent()
		)
	}
}

private fun Project.requireInstallInfo(outputDir: File): CefBuildInfo? {
	fun getRequired() = CefBuildInfo.fromClasspath()
	run {
		if (!File(outputDir, installLock).exists()) {
			return@run
		}
		val installInfo = File(outputDir, "build_meta.json")
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
