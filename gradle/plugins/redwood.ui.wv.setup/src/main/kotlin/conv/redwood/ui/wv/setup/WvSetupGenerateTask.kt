package conv.redwood.ui.wv.setup

import com.google.javascript.jscomp.Compiler
import com.google.javascript.jscomp.CompilerOptions
import com.google.javascript.jscomp.SourceFile
import com.google.javascript.jscomp.SourceMap
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
import org.gradle.kotlin.dsl.property
import java.io.File
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

	@get:Input
	@get:JvmName("getIsDebugBuild")
	val isDebugBuild: Property<Boolean> = objects.property()

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

		val isDebugBuild = isDebugBuild.get()
		val wvSetupBuilder = WvSetupBuilder(isDebugBuild, schemaPackage.get(), classpathUnion)

		wvSetupBuilder.resolveOutputFile(kotlinOutput.get(), "${WvSetupBuilder.OBJECT_NAME}.kt")
			.writeText(wvSetupBuilder.ktSetup) // NOTE: Truncates if file already exists.

		val wvSetupJs = wvSetupBuilder.jsSetup
		val wvSetupJsFile = wvSetupBuilder.resolveOutputFile(assetsOutput.get(), "${WvSetupBuilder.JS_BASE_NAME}.js")
		wvSetupJsFile.writeText(wvSetupJs)

		val wvSetupJsSourceFile = SourceFile.fromCode(wvSetupJsFile.path, wvSetupJs)
		val wvSetupJsDir = wvSetupJsFile.parentFile

		val wvSetupMinJsFilename = "${WvSetupBuilder.JS_BASE_NAME}.min.js"
		val wvSetupMinJsFile = File(wvSetupJsDir, wvSetupMinJsFilename)
		val wvSetupMinJsMapFile = File(wvSetupJsDir, "${WvSetupBuilder.JS_BASE_NAME}.min.js.map")

		val jsCompilerOptions = CompilerOptions()
		jsCompilerOptions.sourceMapOutputPath = wvSetupMinJsMapFile.absolutePath
		jsCompilerOptions.sourceMapLocationMappings = listOf(SourceMap.LocationMapping { loc ->
			// The original JS source should be relative to the output source map file
			loc.substringAfterLast('/').takeIf { it.isNotEmpty() }
		})

		// TODO Properly set up the compiler options

		val jsCompiler = Compiler()
		val result = jsCompiler.compile(
			listOf(), // TODO Don't know what this is for just yet
			listOf(wvSetupJsSourceFile),
			jsCompilerOptions,
		)

		if (result.success) {
			val wvSetupMinJs = jsCompiler.toSource()

			// Output the minified JS
			wvSetupMinJsFile.writeText(wvSetupMinJs) // NOTE: Truncates if file already exists.

			// Output the source map for the minified JS
			wvSetupMinJsMapFile.writeText(buildString {
				result.sourceMap.appendTo(this, wvSetupMinJsFilename)
			})
		}

		// TODO Report errors and warnings (if any)
	}
}
