package build.kt.jvm.app.packaged

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

abstract class JPackageDist : JPackageAbstractTask() {

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
	protected abstract val files: FileOperations

	@get:Inject
	protected abstract val exec: ExecOperations

	@TaskAction
	fun execute() {
		val outputDir = outputDir.get().asFile
		files.delete(outputDir)
		outputDir.mkdirs()

		val appDir = appDir.get()
		val mainJar = mainJar.get()

		val jdepsOutput = ByteArrayOutputStream()
		exec.exec {
			standardOutput = jdepsOutput
			executable = jdepsPath()
			args("--print-module-deps")
			args(appDir.file(mainJar).asFile)
		}.run {
			rethrowFailure()
			assertNormalExitValue()
		}
		val addModulesArg = jdepsOutput.toString().trim()

		val jpackageImageDest = File(temporaryDir, "d")
		val jpackageImageName = spec.appTitle.get()

		exec.exec {
			executable = jpackagePath()
			args = freeArgs.get()
			setUpForJPackage(
				appDir, mainJar,
				addModulesArg,
				jpackageImageDest,
				jpackageImageName,
			)
		}.run {
			rethrowFailure()
			assertNormalExitValue()
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
		appDir: Directory,
		mainJar: String,
		addModulesArg: String,
		jpackageImageDest: File,
		jpackageImageName: String,
	) {
		args("-t", "app-image")

		args("-d", jpackageImageDest)
		args("-n", jpackageImageName)

		args("-i", appDir.asFile)
		args("--main-jar", mainJar)
		mainClass.orNull?.let { args("--main-class", it) }
		args("--add-modules", addModulesArg)

		val spec = spec
		spec.packageVersionCode.orNull?.let { args("--app-version", it) }
		spec.description.orNull?.let { args("--description", it) }
		spec.vendor.orNull?.let { args("--vendor", it) }
		spec.copyright.orNull?.let { args("--copyright", it) }

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

		spec.jvmArgs.get().forEach {
			args("--java-options", it)
		}
	}
}
