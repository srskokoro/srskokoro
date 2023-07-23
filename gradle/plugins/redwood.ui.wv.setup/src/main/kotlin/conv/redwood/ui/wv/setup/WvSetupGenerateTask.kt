package conv.redwood.ui.wv.setup

import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
abstract class WvSetupGenerateTask @Inject constructor(
	private val objects: ObjectFactory,
	private val fsOps: FileSystemOperations,
	private val archiveOps: ArchiveOperations,
) : DefaultTask() {

	@get:Classpath
	abstract val classpath: ConfigurableFileCollection

	@get:Input
	abstract val schemaPackage: Property<String>

	@get:OutputDirectory
	val outputDir = objects.directoryProperty()

	@get:Internal
	val kotlinOutput: Provider<Directory> = outputDir.dir("kotlin")

	@get:Internal
	val assetsOutput: Provider<Directory> = outputDir.dir("assets")

	@TaskAction
	fun execute() {
		fsOps.delete { delete(outputDir) }

		val archiveOps = archiveOps
		val objects = objects

		val classpathUnion = classpath
			.asSequence()
			.mapNotNull {
				if (it.isDirectory) {
					objects.fileTree().from(it)
				} else if (it.isFile && it.name.endsWith(".jar", ignoreCase = true)) {
					archiveOps.zipTree(it)
				} else null
			}
			.reduce { acc, next -> acc + next }

		val wvSetupBuilder = WvSetupBuilder(schemaPackage.get(), classpathUnion)

		wvSetupBuilder.resolveOutputFile(kotlinOutput.get(), "${WvSetupBuilder.OBJECT_NAME}.kt")
			.writeText(wvSetupBuilder.ktSetup)

		val wvSetupJs = wvSetupBuilder.jsSetup
		val wvSetupJsFile = wvSetupBuilder.resolveOutputFile(assetsOutput.get(), "${WvSetupBuilder.OBJECT_NAME}.js")
		wvSetupJsFile.writeText(wvSetupJs)

		val wvSetupJsDir = wvSetupJsFile.parentFile
		// TODO Generate minified version of JS, along with a source map
	}
}
