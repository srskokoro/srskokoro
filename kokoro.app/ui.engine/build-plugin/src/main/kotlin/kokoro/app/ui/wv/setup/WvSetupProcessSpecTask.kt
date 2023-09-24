package kokoro.app.ui.wv.setup

import conv.internal.support.io.asFileTreeVia
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.EmptyFileVisitor
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.RelativePath
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
abstract class WvSetupProcessSpecTask @Inject constructor(
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
		val incomingInputs = LinkedHashMap<RelativePath, FileTreeElement>()
		val incomingOverrides = HashMap<RelativePath, FileTreeElement>()

		classpathInputFiles.visit(object : EmptyFileVisitor() {
			override fun visitFile(visit: FileVisitDetails) {
				if (isMainInputOrOverride(visit.name)) {
					incomingInputs
				} else {
					incomingOverrides
				}.let {
					it[visit.relativePath] = visit
				}
			}
		})

		val sourceLstFiles = LinkedHashMap<RelativePath, FileTreeElement>()
		val sourceInputs = LinkedHashMap<RelativePath, FileTreeElement>()
		val sourceOverrides = HashMap<RelativePath, FileTreeElement>()

		val unlikedSourceJsFiles = HashSet<RelativePath>()
		val unlikedSourceOverrides = HashSet<RelativePath>()

		sourceInputFiles.visit(object : EmptyFileVisitor() {
			override fun visitFile(visit: FileVisitDetails) {
				val relativePath = visit.relativePath
				val name = visit.name
				if (isMainInputOrOverride(name)) {
					if (name.endsWith(".lst")) {
						sourceLstFiles
					} else {
						if (name.endsWith("js") &&
							(name.startsWith(".templ.wv.", name.length - 12) ||
								name.startsWith(".const.wv.", name.length - 12))
						) unlikedSourceJsFiles.add(relativePath)
						sourceInputs
					}
				} else {
					unlikedSourceOverrides.add(relativePath)
					sourceOverrides
				}.let {
					it[relativePath] = visit
				}
			}
		})

		// TODO Process the `*.wv.lst` files
		// TODO Check for unlinked source JS files
		// TODO Check for unlinked source overrides
	}
}

private fun isMainInputOrOverride(name: String): Boolean {
	run {
		if (name.endsWith("js", true)) {
			if (name.startsWith(".wv.", name.length - 6, true)) {
				val i = name.lastIndexOf('.', name.length - (6 + 1)) - 1
				if (i >= 0 && name[i] == '!') return@run
			}
		} else if (name.endsWith("spec", true)) {
			if (name.startsWith("!.wv.", name.length - 9, true)) return@run
		} else if (name.endsWith("lst", true)) {
			if (name.startsWith("!.wv.", name.length - 8, true)) return@run
		}
		return true
	}
	return false
}
