package kokoro.app.ui.wv.setup

import conv.internal.support.io.asFileTreeVia
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import wv
import javax.inject.Inject

@CacheableTask
abstract class WvSetupBuildTask @Inject constructor(
	objects: ObjectFactory,
	archiveOps: ArchiveOperations,
) : DefaultTask() {

	@get:OutputDirectory
	val outputDir: DirectoryProperty = objects.directoryProperty()

	@get:Internal
	val kotlinOutputDir: Provider<Directory> = outputDir.dir("kotlin")

	@get:Internal
	val assetsOutputDir: Provider<Directory> = outputDir.dir("assets")

	// --

	@get:Internal
	val classpath: ConfigurableFileCollection = objects.fileCollection()

	@get:Internal
	val sourceDirectories: ConfigurableFileCollection = objects.fileCollection()

	@get:PathSensitive(PathSensitivity.RELATIVE)
	@get:[IgnoreEmptyDirectories SkipWhenEmpty]
	@get:InputFiles
	internal val classpathInputFiles: FileTree = classpath.asFileTreeVia(objects) {
		if (it.isFile && it.path.endsWith(".${WvSetupExportTask.DEFAULT_EXTENSION}")) {
			archiveOps.zipTree(it)
		} else it
	}.asFileTree.matching {
		include("**/*.wv.js")
		include("**/*.wv.spec")
	}

	@get:PathSensitive(PathSensitivity.RELATIVE)
	@get:[IgnoreEmptyDirectories SkipWhenEmpty]
	@get:InputFiles
	internal val sourceInputFiles: FileTree = sourceDirectories.asFileTree.matching {
		include("**/*.wv.js")
		include("**/*.wv.spec")
		include("**/*.wv.lst")
	}

	fun from(sourceSets: Iterable<KotlinSourceSet>) {
		sourceDirectories.from(fun() = sourceSets.map { it.wv.sourceDirectories })
	}

	@TaskAction
	fun execute() {
		val analysis = WvSetupSourceAnalysis()
		analysis.loadClasspathInputFiles(classpathInputFiles)
		analysis.loadSourceInputFiles(sourceInputFiles)

		val entries = analysis.entries
		for (lst in analysis.lstEntries) {
			val lstState = WvSetupBuilderState(lst, entries)
			// TODO Process `lstState`
		}
	}
}
