package kokoro.app.ui.wv.setup

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.work.InputChanges
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import wv
import javax.inject.Inject

@CacheableTask
abstract class WvSetupGenerateTask @Inject constructor(
	objects: ObjectFactory,
) : DefaultTask() {

	@get:OutputDirectory
	val outputDir: DirectoryProperty = objects.directoryProperty()

	@get:Internal
	val sourceDirectories: ConfigurableFileCollection = objects.fileCollection()

	@get:PathSensitive(PathSensitivity.RELATIVE)
	@get:[IgnoreEmptyDirectories SkipWhenEmpty]
	@get:InputFiles
	internal val inputFiles: FileTree = sourceDirectories.asFileTree.matching {
		include("**/*.const.wv.js")
		exclude("**/*!.const.wv.js")

		include("**/*.templ.wv.js")
		exclude("**/*!.templ.wv.js")

		include("**/*.wv.lst")
		exclude("**/*!.wv.lst")
	}

	fun from(sourceSet: KotlinSourceSet) {
		sourceDirectories.from(sourceSet.wv.sourceDirectories)
	}

	@TaskAction
	fun execute(inputChanges: InputChanges) {
		// TODO!
	}
}
