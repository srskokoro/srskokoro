package kokoro.app.ui.wv.setup

import com.google.javascript.jscomp.BlackHoleErrorManager
import com.google.javascript.jscomp.CommandLineRunner
import com.google.javascript.jscomp.CompilationLevel
import com.google.javascript.jscomp.Compiler
import com.google.javascript.jscomp.CompilerOptions
import com.google.javascript.jscomp.PropertyRenamingPolicy
import com.google.javascript.jscomp.SourceFile
import com.google.javascript.jscomp.VariableRenamingPolicy
import com.google.javascript.jscomp.WarningLevel
import com.google.javascript.jscomp.parsing.parser.FeatureSet
import com.google.javascript.rhino.StaticSourceFile.SourceKind
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import java.io.File
import java.util.LinkedList

internal fun minifyJs(
	jsSourceFile: SourceFile,
	minJsFile: File,
	minJsMapFile: File,
	externEntries: LinkedList<WvSetupSourceAnalysis.Entry>,
	jsCompilerOptions: CompilerOptions,
	logger: Logger,
) {
	val jsCompiler = Compiler(BlackHoleErrorManager())

	val result = jsCompiler.compile(
		CommandLineRunner.getBuiltinExterns(jsCompilerOptions.environment),
		ArrayList<SourceFile>().also { arr ->
			externEntries.mapTo(arr) { SourceFile.fromCode(it.path, it.content, SourceKind.EXTERN) }
			arr.add(jsSourceFile)
		},
		jsCompilerOptions,
	)

	if (result.errors.size + result.warnings.size > 0) {
		val formatter = jsCompilerOptions.errorFormat.toFormatter(
			jsCompiler, jsCompilerOptions.shouldColorizeErrorOutput())

		// NOTE: The "e:" and "w:" prefixes are necessary for IntelliJ IDEA
		// (or Android Studio) to mark each log entry as a separate error or
		// warning node in the IDE's build tool window.

		if (logger.isErrorEnabled) for (error in result.errors)
			logger.error("e: ${formatter.formatError(error)}")
		if (logger.isWarnEnabled) for (error in result.warnings)
			logger.warn("w: ${formatter.formatWarning(error)}")
	}

	if (!result.success) throw GradleException(
		"JS minification failure. See log for more details.")

	// Output the minified JS
	val minJsStr: String = jsCompiler.toSource()
	minJsFile.writeText(minJsStr) // NOTE: Truncates if file already exists.

	// Output the source map for the minified JS
	minJsMapFile.writeText(buildString {
		result.sourceMap.appendTo(this, minJsMapFile.name)
	})
}

internal fun setUpForMinifyJs(options: CompilerOptions) {
	with(CompilationLevel.ADVANCED_OPTIMIZATIONS) {
		setOptionsForCompilationLevel(options)
		setTypeBasedOptimizationOptions(options)
	}

	WarningLevel.VERBOSE.setOptionsForWarningLevel(options)

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
