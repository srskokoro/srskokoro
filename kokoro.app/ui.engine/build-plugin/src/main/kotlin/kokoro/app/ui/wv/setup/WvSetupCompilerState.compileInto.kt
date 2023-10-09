package kokoro.app.ui.wv.setup

import com.google.javascript.jscomp.CompilerOptions
import com.google.javascript.jscomp.SourceFile
import com.google.javascript.jscomp.SourceMap
import conv.internal.support.removeLast
import kokoro.app.ui.wv.setup.GenerationUtils.appendIdentifierPartAfterStart
import kokoro.app.ui.wv.setup.GenerationUtils.appendIdentifierStart
import kokoro.app.ui.wv.setup.GenerationUtils.appendInDqString
import kokoro.app.ui.wv.setup.GenerationUtils.appendKtPackageHeader
import kokoro.app.ui.wv.setup.WvSetupSourceAnalysis.N
import kokoro.app.ui.wv.setup.WvSetupSourceAnalysis.S
import org.gradle.api.logging.Logger
import java.io.File
import java.util.regex.Pattern

internal fun WvSetupCompilerState.compileInto(ktOutputDir: File, jsOutputDir: File, logger: Logger) {
	val lst = lst
	val baseName = lst.name.removeLast(N.D_WV_LST)

	// --

	val kt = StringBuilder("@file:Suppress(\"ACTUAL_WITHOUT_EXPECT\")\n\n")

	val pathSegments = lst.relativePath.segments
	if (pathSegments.size >= 2) {
		appendKtPackageHeader(kt, pathSegments, 0, pathSegments.size - 1)
		kt.appendLine()
	}

	kt.append("public actual fun ")
	appendIdentifierStart(kt, baseName)
	kt.append("_wv_getId(templPath: String): Int = when (templPath) {\n")

	val js = StringBuilder("'use strict';(function(){\n")
	stitchInto(kt, js)
	js.append("})()\n")

	kt.appendLine("\telse -> -1")
	kt.appendLine('}')

	// --
	// Output the built strings into their respective files

	val packagePath = lst.packagePath

	val ktPackageDir = File(ktOutputDir, packagePath)
	val jsPackageDir = File(jsOutputDir, packagePath)

	ktPackageDir.mkdirs()
	jsPackageDir.mkdirs()

	val ktFile = File(ktPackageDir, "$baseName${S.D_WV_KT}")
	val jsFile = File(jsPackageDir, "$baseName${S.D_WV_JS}")

	val ktStr = kt.toString()
	val jsStr = js.toString()

	ktFile.writeText(ktStr) // NOTE: Truncates if file already exists.
	jsFile.writeText(jsStr)

	// --
	// JS minification

	val jsSourceFile = SourceFile.fromCode(jsFile.toPath().toUri().toASCIIString(), jsStr)

	val jsCompilerOptions = CompilerOptions()
	setUpForMinifyJs(jsCompilerOptions)

	val minJsFile = File(jsPackageDir, "$baseName${S.D_WV_D}min.js")
	val minJsMapFile = File(jsPackageDir, "$baseName${S.D_WV_D}min.js.map")

	jsCompilerOptions.sourceMapOutputPath = minJsMapFile.absolutePath
	jsCompilerOptions.sourceMapLocationMappings = listOf(SourceMap.LocationMapping { loc ->
		// The original JS source should be relative to the output source map file
		loc.substringAfterLast('/').takeIf { it.isNotEmpty() }
	})

	minifyJs(
		jsSourceFile,
		minJsFile = minJsFile,
		minJsMapFile = minJsMapFile,
		externEntries,
		jsCompilerOptions,
		logger,
	)
}

private fun WvSetupCompilerState.stitchInto(ktCases: StringBuilder, jsBuilder: StringBuilder) {
	@Suppress("UnnecessaryVariable") val kt = ktCases
	@Suppress("UnnecessaryVariable") val js = jsBuilder

	for (entry in headEntries) {
		val effective = entry.getEffectiveEntry()
		appendJsEntryHeaderLine(js, effective)
		appendJsEntryContent(js, effective)
	}

	var nextTemplId = 0
	for ((packagePath, packageEntry) in packageEntries) {
		js.appendLine()
		js.append(";//+ Source package: ")
		js.appendLine(packagePath)
		js.append("(function(){\n")

		for (entry in packageEntry.constEntries) {
			appendJsEntryHeaderLine(js, entry)
			js.appendLine(entry.content) // NOTE: No overrides (supposedly).
		}

		for (entry in packageEntry.templEntries) {
			val id = nextTemplId++

			kt.append('\t')
			kt.append('"')
			appendInDqString(kt, entry.path)
			kt.append("\" -> ")
			kt.appendLine(id)

			val baseName = entry.name.removeLast(N.D_WV_TEMPL_JS)

			val effective = entry.getEffectiveEntry()
			appendJsEntryHeaderLine(js, effective)

			js.append("const t_")
			appendIdentifierPartAfterStart(js, baseName)
			js.append(" = ")
			js.append(id)
			js.appendLine(';')

			js.append("const s_")
			appendIdentifierPartAfterStart(js, baseName)
			js.append(" = ")
			js.appendLine("Symbol();")

			js.appendLine("(function(){")
			appendJsEntryContent(js, effective)
			js.appendLine("})()")
		}

		js.append("\n})()\n")
	}
	js.appendLine()

	for (entry in tailEntries) {
		val effective = entry.getEffectiveEntry()
		appendJsEntryHeaderLine(js, effective)
		appendJsEntryContent(js, effective)
	}
}

private fun appendJsEntryHeaderLine(js: StringBuilder, entry: WvSetupSourceAnalysis.Entry) {
	js.appendLine()
	js.append(";//+ Source: ")
	js.appendLine(entry.path)
}

// language=RegExp
private object RegexWvJsDirectives {
	const val flags = Pattern.DOTALL

	// See, https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#line_terminators
	const val nl = """[\n\r\u2028\u2029]"""

	const val dir_char = '%'

	const val ig_ungrouped = """\s++|//(?!$dir_char).*?(?:$nl++|\z)|/\*.*?(?:\*/|\z)"""

	@Suppress("RegExpUnnecessaryNonCapturingGroup")
	const val ig_stat = """(?:$ig_ungrouped|;|\z)"""

	const val heading = """$ig_stat*+"""

	const val dir_entry_g_cmd = 1
	const val dir_entry_g_tail = 2
	const val dir_entry = """\G//$dir_char(\S++).*?(?:$nl++|\z)($ig_stat*+)"""

	val heading_P: Pattern = Pattern.compile(heading, flags)
	val dir_entry_P: Pattern = Pattern.compile(dir_entry, flags)
}

private fun appendJsEntryContent(js: StringBuilder, entry: WvSetupSourceAnalysis.Entry) {
	val input = entry.content

	val m = RegexWvJsDirectives.heading_P.matcher(input)
	if (!m.lookingAt()) throw AssertionError("Expected to match anything (including the empty string).\n- Input file: ${entry.sourceFile ?: entry.path}")

	var last = m.end()
	js.append(input, 0, last)

	var includeBase = 0 // -1 means prepend; 1 means append;
	m.usePattern(RegexWvJsDirectives.dir_entry_P)

	while (m.find()) {
		last = m.end()
		val tail_i = m.start(RegexWvJsDirectives.dir_entry_g_tail)
		js.append(input, tail_i, last)

		val cmd_i = m.start(RegexWvJsDirectives.dir_entry_g_cmd)
		if (input.startsWith("prepend-base", cmd_i)) {
			if (includeBase == 0) includeBase = -1
		} else if (input.startsWith("append-base", cmd_i)) {
			if (includeBase == 0) includeBase = 1
		}
	}

	if (includeBase < 0) entry.base?.let { appendJsEntryContent(js, it) }

	js.append(input, last, input.length)
	js.appendLine()

	if (includeBase > 0) entry.base?.let { appendJsEntryContent(js, it) }
}
