package build.kt.jvm.app.packaged

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import java.io.File
import javax.inject.Inject

abstract class JPackageDist : AbstractJPackageTask() {

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
	 * The main JAR of the application containing the main class (specified as a
	 * path relative to the [appDir] path).
	 */
	@get:Input
	abstract val mainJar: Property<String>

	@get:OutputDirectory
	abstract val outputDir: DirectoryProperty

	// --

	@get:Inject
	protected abstract val files: FileOperations

	@get:Inject
	protected abstract val exec: ExecOperations

	@TaskAction
	fun execute() {
		spec.validate(logger)

		val outputDir = outputDir.get().asFile
		files.delete(outputDir)
		outputDir.mkdirs()

		val jpackageImageDest = File(temporaryDir, "d")
		val jpackageImageName = spec.appTitle.get()

		exec.exec {
			executable = jpackagePath()
			args = freeArgs.get()
			setUpForJPackage(jpackageImageDest, jpackageImageName)
		}.run {
			assertNormalExitValue()
			rethrowFailure()
		}

		for (s in File(jpackageImageDest, jpackageImageName).listFiles()!!) {
			// Necessary since `jpackage` seems to output executable files as
			// read-only, which may prevent the JVM from deleting them.
			s.setWritable(true) // Allow the JVM (and Gradle) to delete it.

			File(outputDir, s.name).let { d ->
				if (!s.renameTo(d))
					throw FileSystemException(s, d, "Failed to move file.")
			}
		}

		val files = files
		files.delete(jpackageImageDest)

		files.copy {
			val spec = spec
			spec.licenseFile.orNull?.let { licenseFile ->
				from(licenseFile) {
					rename { spec.licenseFileName.get() }
					into("legal")
				}
			}
			from(spec.bundleAdditions)
			into(outputDir)
		}
	}

	private fun ExecSpec.setUpForJPackage(
		jpackageImageDest: File,
		jpackageImageName: String,
	) {
		args("-t", "app-image")

		args("-d", jpackageImageDest)
		args("-n", jpackageImageName)

		args("-i", appDir.get().asFile)
		args("--main-jar", mainJar.get())

		val spec = spec
		val res = spec.jpackageResources.get()
		val icon: File

		when (JPackagePlatform.current) {
			JPackagePlatform.WINDOWS -> {
				icon = res.file("icon-win.ico").asFile
			}
			JPackagePlatform.MACOS -> {
				icon = res.file("icon-mac.icns").asFile
			}
			JPackagePlatform.LINUX -> {
				icon = res.file("icon-linux.png").asFile
			}
		}

		if (icon.isFile) args("--icon", icon)
	}
}
