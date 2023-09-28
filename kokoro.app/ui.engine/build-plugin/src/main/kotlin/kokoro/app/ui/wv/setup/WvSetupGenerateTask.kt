package kokoro.app.ui.wv.setup

import conv.internal.support.removeLast
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileType
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
import org.gradle.work.ChangeType
import org.gradle.work.FileChange
import org.gradle.work.InputChanges
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import wv
import java.io.File
import javax.inject.Inject
import kotlin.io.path.deleteExisting

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
		val forGeneration = LinkedHashSet<String>()

		val outputDir = outputDir.get()
		for (change in inputChanges.getFileChanges(inputFiles)) {
			if (change.fileType == FileType.DIRECTORY) continue

			val entry = change.normalizedPath
			// TODO Optimize case checks
			if (entry.endsWith(".const.wv.js")) {
				val target = outputDir.file("${entry.removeLast(2)}kt").asFile
				if (handleForGeneration(target, change, forGeneration)) {
					generateForConstWvJs(target, change)
				}
			} else if (entry.endsWith(".templ.wv.js")) {
				val target = outputDir.file("${entry.removeLast(2)}kt").asFile
				if (handleForGeneration(target, change, forGeneration)) {
					generateForTemplWvJs(target, change)
				}
			} else if (entry.endsWith(".wv.lst")) {
				val target = outputDir.file("${entry}.kt").asFile
				if (handleForGeneration(target, change, forGeneration)) {
					generateForWvLst(target, change)
				}
			} else {
				throw AssertionError("Unknown input file format: ${change.file}")
			}
		}
	}
}

private fun handleForGeneration(target: File, change: FileChange, forGeneration: MutableSet<String>): Boolean {
	if (change.changeType == ChangeType.REMOVED) {
		if (change.normalizedPath !in forGeneration && !target.delete()) {
			// Let the following throw a pretty error message
			target.toPath().deleteExisting()
		}
		return false // Done.
	}
	if (!forGeneration.add(change.normalizedPath)) {
		throw E_DuplicateSourceEntry(change)
	}
	return true
}

private fun generateForConstWvJs(target: File, change: FileChange) {
	// TODO Implement
}

private fun generateForTemplWvJs(target: File, change: FileChange) {
	// TODO Implement
}

private fun generateForWvLst(target: File, change: FileChange) {
	// TODO Implement
}

internal fun E_DuplicateSourceEntry(change: FileChange) =
	E_DuplicateSourceEntry(change.normalizedPath, change.file)
