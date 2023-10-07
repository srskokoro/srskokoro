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
		appendJsEntryHeaderLine(js, entry)
		js.appendLine(entry.content)
	}

	var nextTemplId = 0
	for ((packagePath, packageEntry) in packageEntries) {
		js.append(";// Source package: ")
		js.appendLine(packagePath)
		js.appendLine("(function(){")
		for (entry in packageEntry.constEntries) {
			appendJsEntryHeaderLine(js, entry)
			js.appendLine(entry.content)
		}
		for (entry in packageEntry.templEntries) {
			val id = nextTemplId++

			appendJsEntryHeaderLine(js, entry)
			val baseName = entry.name.removeLast(N.D_TEMPL_WV_JS)

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
			js.appendLine(entry.content)
			js.appendLine("})()")

			kt.append('\t')
			kt.append('"')
			appendInDqString(kt, entry.path)
			kt.append("\" -> ")
			kt.appendLine(id)
		}
		js.appendLine("})()")
	}

	for (entry in tailEntries) {
		appendJsEntryHeaderLine(js, entry)
		js.appendLine(entry.content)
	}
}

private fun appendJsEntryHeaderLine(sb: StringBuilder, entry: WvSetupSourceAnalysis.Entry) {
	sb.append(";// Source: ")
	sb.appendLine(entry.path)
}
