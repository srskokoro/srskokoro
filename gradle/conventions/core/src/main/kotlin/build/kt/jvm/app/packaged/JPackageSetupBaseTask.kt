package build.kt.jvm.app.packaged

import build.api.file.file
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.File
import javax.inject.Inject

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class JPackageSetupBaseTask : JPackageBaseTask() {

	@get:Internal
	abstract val type: String

	@get:Internal
	abstract val iconResFileName: String

	companion object {
		const val RES_ICON_WIN_FILE = "icon-win.ico"
		const val RES_ICON_MAC_FILE = "icon-mac.icns"
		const val RES_ICON_LINUX_FILE = "icon-linux.png"
	}

	// --

	@get:Internal
	abstract val appImage: DirectoryProperty

	init {
		@Suppress("LeakingThis")
		// NOTE: While we could've used `@InputDirectory`, that apparently
		// implies `@IgnoreEmptyDirectories`, which we don't want.
		inputs.files(appImage.asFileTree)
			.withPropertyName(::appImage.name)
			.withPathSensitivity(PathSensitivity.RELATIVE)
	}

	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	// --

	@get:Inject
	protected abstract val files: FileOperations

	@get:Inject
	protected abstract val exec: ExecOperations

	@TaskAction
	open fun execute() {
		val outputFile = outputFile.file
		files.delete(outputFile)
		outputFile.parentFile.mkdirs()

		// --

		initJPackageExecArgs()

		val tmpDestDir = temporaryDir
		files.delete(tmpDestDir)

		jpackageExecArgs.apply {
			args("-d", tmpDestDir.path)
		}

		exec.exec {
			executable = jpackagePath()
			args(jpackageExecArgs)
		}.run {
			rethrowFailure()
			assertNormalExitValue()
		}

		// --

		tmpDestDir.listFiles()!!.single().let { x ->
			// Necessary since `jpackage` seems to output executable files as
			// read-only, which may prevent the JVM from deleting/moving them.
			x.setWritable(true)

			if (!x.renameTo(outputFile)) throw FileSystemException(
				x, outputFile,
				"Failed to move file.",
			)
		}

		files.delete(tmpDestDir)
	}

	override fun initJPackageExecArgs() {
		super.initJPackageExecArgs()
		jpackageExecArgs.apply {
			args("-t", type)

			val appImage = appImage.file
			args("--app-image", appImage.path)

			val spec = spec
			File(appImage, JPackageDist.DIR_LEGAL + File.separatorChar + spec.licenseFileName.get()).run {
				if (isFile) args("--license-file", path)
			}
			File(spec.jpackageResources.file, iconResFileName).run {
				if (isFile) args("--icon", path)
			}
		}
	}
}
