package kokoro.app.ui.wv.setup

import conv.internal.support.removeLast
import kokoro.app.ui.wv.setup.WvSetupSourceAnalysis.N
import kokoro.app.ui.wv.setup.WvSetupSourceAnalysis.S
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
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

			val path = change.normalizedPath
			if (path.endsWith(S.D_CONST_WV_JS)) {
				val target = outputDir.file("${path.removeLast(N.JS)}kt").asFile
				if (handleForGeneration(target, change, forGeneration)) {
					generateForConstWvJs(target, change)
				}
			} else if (path.endsWith(S.D_TEMPL_WV_JS)) {
				val target = outputDir.file("${path.removeLast(N.JS)}kt").asFile
				if (handleForGeneration(target, change, forGeneration)) {
					generateForTemplWvJs(target, change)
				}
			} else if (path.endsWith(S.D_WV_LST)) {
				val target = outputDir.file("${path.removeLast(N.LST)}kt").asFile
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
	val path = change.normalizedPath

	val pathSegments = path.split('/')
	val pathSegments_last = pathSegments.size - 1

	val kt = StringBuilder()
	if (pathSegments_last >= 1) {
		kt.append("package ")
		appendKotlinIdentifier(kt, pathSegments[0])
		for (i in 1 until (pathSegments.size - 1)) {
			kt.append('.')
			appendKotlinIdentifier(kt, pathSegments[i])
		}
		kt.appendLine()
	}
	kt.appendLine()

	val baseName = pathSegments[pathSegments_last].removeLast(N.D_TEMPL_WV_JS)
	checkBaseName(baseName, change)

	kt.append("const val t_")
	kt.append(baseName)
	kt.append(" = \"")
	kt.append(path)
	kt.appendLine('"')

	target.writeText(kt.toString()) // NOTE: Truncates if file already exists.
}

private fun generateForWvLst(target: File, change: FileChange) {
	val path = change.normalizedPath

	val pathSegments = path.split('/')
	val pathSegments_last = pathSegments.size - 1

	val kt = StringBuilder()
	if (pathSegments_last >= 1) {
		kt.append("package ")
		appendKotlinIdentifier(kt, pathSegments[0])
		for (i in 1 until (pathSegments.size - 1)) {
			kt.append('.')
			appendKotlinIdentifier(kt, pathSegments[i])
		}
		kt.appendLine()
	}
	kt.appendLine()

	val baseName = pathSegments[pathSegments_last].removeLast(N.D_WV_LST)
	checkBaseName(baseName, change)

	kt.append("public expect fun ")
	kt.append(baseName)
	kt.appendLine("_wv_getId(templPath: String): Int")

	target.writeText(kt.toString()) // NOTE: Truncates if file already exists.
}

private fun checkBaseName(name: String, change: FileChange) {
	if (!isSimpleKotlinIdentifier(name)) {
		throw InvalidUserDataException("The base name of the file should be a simple Kotlin identifier.\n- Input file: ${change.file}")
	}
}

private fun E_DuplicateSourceEntry(change: FileChange) =
	E_DuplicateSourceEntry(change.normalizedPath, change.file)
