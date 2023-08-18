package conv.redwood.ui.wv.setup

import com.google.javascript.jscomp.BlackHoleErrorManager
import com.google.javascript.jscomp.CheckLevel
import com.google.javascript.jscomp.CommandLineRunner
import com.google.javascript.jscomp.CompilationLevel
import com.google.javascript.jscomp.Compiler
import com.google.javascript.jscomp.CompilerOptions
import com.google.javascript.jscomp.DiagnosticGroups
import com.google.javascript.jscomp.PropertyRenamingPolicy
import com.google.javascript.jscomp.SourceFile
import com.google.javascript.jscomp.SourceMap
import com.google.javascript.jscomp.VariableRenamingPolicy
import com.google.javascript.jscomp.WarningLevel
import com.google.javascript.jscomp.parsing.parser.FeatureSet
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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

		val wvSetupJsSourceFile = SourceFile.fromCode(wvSetupJsFile.toPath().toUri().toASCIIString(), wvSetupJs)
		val wvSetupJsDir = wvSetupJsFile.parentFile

		val wvSetupMinJsFilename = "${WvSetupBuilder.JS_BASE_NAME}.min.js"
		val wvSetupMinJsFile = File(wvSetupJsDir, wvSetupMinJsFilename)
		val wvSetupMinJsMapFile = File(wvSetupJsDir, "${WvSetupBuilder.JS_BASE_NAME}.min.js.map")

		val jsCompilerOptions = CompilerOptions()
		setUpJsCompilerOptions(jsCompilerOptions)

		jsCompilerOptions.sourceMapOutputPath = wvSetupMinJsMapFile.absolutePath
		jsCompilerOptions.sourceMapLocationMappings = listOf(SourceMap.LocationMapping { loc ->
			// The original JS source should be relative to the output source map file
			loc.substringAfterLast('/').takeIf { it.isNotEmpty() }
		})

		val jsCompiler = Compiler(BlackHoleErrorManager())
		val result = jsCompiler.compile(
			CommandLineRunner.getBuiltinExterns(jsCompilerOptions.environment),
			listOf(wvSetupJsSourceFile),
			jsCompilerOptions,
		)

		if (result.errors.size + result.warnings.size > 0) {
			val formatter = jsCompilerOptions.errorFormat.toFormatter(
				jsCompiler, jsCompilerOptions.shouldColorizeErrorOutput())

			// NOTE: The "e:" and "w:" prefixes are necessary for IntelliJ IDEA
			// (or Android Studio) to mark each log entry as a separate error or
			// warning node in the IDE's build tool window.

			val logger = logger
			if (logger.isErrorEnabled) for (error in result.errors)
				logger.error("e: ${formatter.formatError(error)}")
			if (logger.isWarnEnabled) for (error in result.warnings)
				logger.warn("w: ${formatter.formatWarning(error)}")
		}

		if (!result.success) throw GradleException(
			"JS minification failure. See log for more details.")

		val wvSetupMinJs = jsCompiler.toSource()

		// Output the minified JS
		wvSetupMinJsFile.writeText(wvSetupMinJs) // NOTE: Truncates if file already exists.

		// Output the source map for the minified JS
		wvSetupMinJsMapFile.writeText(buildString {
			result.sourceMap.appendTo(this, wvSetupMinJsFilename)
		})
	}
}

private fun setUpJsCompilerOptions(options: CompilerOptions) {
	with(CompilationLevel.ADVANCED_OPTIMIZATIONS) {
		setOptionsForCompilationLevel(options)
		setTypeBasedOptimizationOptions(options)
	}

	WarningLevel.VERBOSE.setOptionsForWarningLevel(options)

	options.checkSymbols = false
//	options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.OFF)

	options.setRenamingPolicy(VariableRenamingPolicy.LOCAL, PropertyRenamingPolicy.ALL_UNQUOTED)
	options.setOutputCharset(Charsets.UTF_8)

	// Injects polyfills for ES2015+ library classes and methods used in source.
	// See also,
	// - https://github.com/google/closure-compiler/wiki/Polyfills
	// - [com.google.javascript.jscomp.CommandLineRunner.Flags.rewritePolyfills]
	//
	// TODO Properly assess this option.
	options.rewritePolyfills = options.languageIn.toFeatureSet().contains(FeatureSet.ES2015)
}
