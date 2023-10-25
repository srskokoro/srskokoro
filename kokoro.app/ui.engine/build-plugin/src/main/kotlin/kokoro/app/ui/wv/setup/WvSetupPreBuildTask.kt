package kokoro.app.ui.wv.setup

import conv.internal.support.removeLast
import kokoro.app.ui.wv.setup.GenerationUtils.appendIdentifierPartAfterStart
import kokoro.app.ui.wv.setup.GenerationUtils.appendIdentifierStart
import kokoro.app.ui.wv.setup.GenerationUtils.appendInDqString
import kokoro.app.ui.wv.setup.GenerationUtils.appendKtPackageHeader
import kokoro.app.ui.wv.setup.WvSetup.N
import kokoro.app.ui.wv.setup.WvSetup.S
import kokoro.app.ui.wv.setup.WvSetup.WV_LST_getId
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
import java.nio.file.Files
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.io.path.deleteExisting
import kotlin.math.max
import kotlin.math.min

@CacheableTask
abstract class WvSetupPreBuildTask @Inject constructor(
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
		include("**/*${S.D_WV_CONST_JS}")
		exclude("**/*!${S.D_WV_CONST_JS}")

		include("**/*${S.D_WV_UNIT_JS}")
		exclude("**/*!${S.D_WV_UNIT_JS}")

		include("**/*${S.D_WV_LST}")
		exclude("**/*!${S.D_WV_LST}")
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
			if (path.endsWith(S.D_WV_CONST_JS)) {
				val target = outputDir.file("${path.removeLast(N.JS)}${S.KT}").asFile
				if (handleForGeneration(target, change, forGeneration)) {
					generateForWvConstJs(target, change)
				}
			} else if (path.endsWith(S.D_WV_UNIT_JS)) {
				val target = outputDir.file("${path.removeLast(N.JS)}${S.KT}").asFile
				if (handleForGeneration(target, change, forGeneration)) {
					generateForWvUnitJs(target, change)
				}
			} else if (path.endsWith(S.D_WV_LST)) {
				// WARNING: Should not have the same name as the file containing
				// the `actual` declarations, so that we can add non-`expect`
				// declarations without worrying about an equivalent class file
				// generated clashing with the class file generated for the file
				// containing the `actual` declarations.
				val target = outputDir.file("${path.removeLast(N.LST + 1)}!.${S.KT}").asFile
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
	val parentFile = target.parentFile
	if (!parentFile.mkdirs() && !parentFile.exists()) {
		// Let the following throw a pretty error message
		Files.createDirectories(parentFile.toPath())
	}
	return true
}

// language=RegExp
private object RegexConstWvJs {
	const val flags = Pattern.DOTALL

	// See, https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#line_terminators
	const val nl = """[\n\r\u2028\u2029]"""

	const val ig_ungrouped = """\s++|//.*?(?:$nl++|\z)|/\*.*?(?:\*/|\z)"""

	@Suppress("RegExpUnnecessaryNonCapturingGroup")
	const val ig = """(?:$ig_ungrouped)"""

	@Suppress("RegExpUnnecessaryNonCapturingGroup")
	const val ig_stat = """(?:$ig_ungrouped|;|\z)"""

	const val heading = """$ig_stat*+"""

	const val num_lenient = """(?=[\d.])""" +
		"""(?:0\w*+|[1-9][\d_]*+)?+""" +
		"""(?:\.[\d_]++)?+""" +
		"""(?:[Ee][+-]?+[\d_]++)?+""" +
		"""\w*+"""

	const val allowed_name = """[A-Za-z_]\w{2,}+"""

	const val const_entry_g_name = 1
	const val const_entry_g_num = 2
	const val const_entry = """const$ig++($allowed_name)$ig*+=$ig*+(?:($num_lenient)|Symbol$ig*+\($ig*+\))$ig_stat*+"""

	val heading_P: Pattern = Pattern.compile(heading, flags)
	val const_entry_P: Pattern = Pattern.compile(const_entry, flags)
}

private fun generateForWvConstJs(target: File, change: FileChange) {
	val path = change.normalizedPath
	val pathSegments = path.split('/')

	val kt = StringBuilder()

	appendKtPackageHeader(kt, pathSegments, 0, pathSegments.size - 1)
	kt.appendLine()

	// --

	val input = change.file.readText()

	val m = RegexConstWvJs.heading_P.matcher(input)
	if (!m.lookingAt()) throw AssertionError("Expected to match anything (including the empty string).\n- Input file: ${change.file}")

	var last = m.end()
	m.usePattern(RegexConstWvJs.const_entry_P)

	while (m.find()) {
		if (last != m.start()) throw E_UnsupportedJsConstDecl(change, input, errorAt = last)
		last = m.end()

		val num_i = m.start(RegexConstWvJs.const_entry_g_num)
		if (num_i > 0) {
			val num_e = m.end(RegexConstWvJs.const_entry_g_num)
			checkJsNumForKtOutput(input, num_i, num_e, change)

			kt.append("public const val `")
			kt.append(
				input,
				m.start(RegexConstWvJs.const_entry_g_name),
				m.end(RegexConstWvJs.const_entry_g_name),
			)
			kt.append("` = ")
			kt.append(input, num_i, num_e)
			kt.appendLine()
		}
	}

	// --

	target.writeText(kt.toString()) // NOTE: Truncates if file already exists.
}

private fun generateForWvUnitJs(target: File, change: FileChange) {
	val path = change.normalizedPath
	val pathSegments = path.split('/')

	val kt = StringBuilder()

	val pathSegments_last = pathSegments.size - 1
	appendKtPackageHeader(kt, pathSegments, 0, pathSegments_last)
	kt.appendLine()

	val baseName = pathSegments[pathSegments_last].removeLast(N.D_WV_UNIT_JS)

	kt.append("public const val ")
	appendIdentifierStart(kt, baseName)
	kt.append("_wv_unit = \"")
	appendInDqString(kt, path)
	kt.appendLine('"')

	target.writeText(kt.toString()) // NOTE: Truncates if file already exists.
}

private fun generateForWvLst(target: File, change: FileChange) {
	val path = change.normalizedPath
	val pathSegments = path.split('/')

	val kt = StringBuilder("@file:Suppress(\"NO_ACTUAL_FOR_EXPECT\")\n\n")

	val pathSegments_last = pathSegments.size - 1
	if (appendKtPackageHeader(kt, pathSegments, 0, pathSegments_last)) {
		kt.appendLine()
	}

	val baseName = pathSegments[pathSegments_last].removeLast(N.D_WV_LST)

	kt.append("public const val ")
	appendIdentifierStart(kt, baseName)
	kt.append("_wv_js = \"")
	appendInDqString(kt, path.removeLast(N.LST))
	kt.appendLine("${S.JS}\"")
	kt.appendLine()

	kt.append("public expect fun ")
	appendIdentifierStart(kt, baseName)
	kt.appendLine(WV_LST_getId)

	target.writeText(kt.toString()) // NOTE: Truncates if file already exists.
}

private fun E_DuplicateSourceEntry(change: FileChange) =
	E_DuplicateSourceEntry(change.normalizedPath, change.file)

// --

private fun E_msg_Js(change: FileChange, loadedContents: String, errorAt: Int) =
	"Error: " + change.file + ':' + getJsLineAndColumn(loadedContents, index = errorAt)

private fun E_UnsupportedJsConstDecl(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Unsupported javascript construct.\n" +
		"- Only `const` declarations of numeric or symbol literals are supported.\n" +
		"- Also, the name of the `const` declaration must match the Java RegExp `" + RegexConstWvJs.allowed_name + "`."
)

private fun getJsLineAndColumn(contents: String, index: Int): String {
	var line = 1
	var line_i = 0
	val n = min(contents.length, index + 1)
	if (n > 0) {
		var prev_c = contents[0]
		var i = 0
		while (++i < n) {
			run {
				when (prev_c) {
					// See, https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#line_terminators
					'\n', '\u2028', '\u2029' -> {}
					'\r' -> if (contents[i] == '\n') return@run
					else -> return@run
				}
				line++
				line_i = i
			}
			prev_c = contents[i]
		}
	}
	val col = max(n - line_i, 1)
	return "$line:$col"
}

// --

private const val ZERO_BYTE = 0.toByte()

private object DigitFlags {
	val F_BIN = 0b001.toByte()
	val F_DEC = 0b010.toByte()
	val F_HEX = 0b100.toByte()

	const val MAP_START = '+'.code
	const val MAP_END = 'f'.code + 1

	val MAP: ByteArray

	init {
		val map = ByteArray(MAP_END - MAP_START)
		map['0'.code - MAP_START] = (F_BIN or F_DEC or F_HEX)
		map['1'.code - MAP_START] = (F_BIN or F_DEC or F_HEX)
		for (i in ('2'.code - MAP_START)..('9'.code - MAP_START)) {
			map[i] = (F_DEC or F_HEX)
		}
		for (i in ('A'.code - MAP_START)..('F'.code - MAP_START)) {
			map[i] = F_HEX
		}
		for (i in ('a'.code - MAP_START)..('f'.code - MAP_START)) {
			map[i] = F_HEX
		}
		MAP = map
	}
}

private fun checkJsNumForKtOutput(input: String, checkStart: Int, checkEnd: Int, from: FileChange) {
	@Suppress("UnnecessaryVariable") val end = checkEnd
	var i = checkStart
	if (i < 0 || i >= end || end > input.length) throw E_JsNumAssertionFailed(from, input, errorAt = 1)

	val flag: Byte
	var dotFound = false
	var expFound_i = 0

	var prev_c = '\u0000'
	var c = input[i]
	if (c == '0') {
		if (++i >= end) return
		c = input[i]
		flag = when (c) {
			'_' -> throw E_JsNumSepForbiddenAfterLeadingZero(from, input, errorAt = checkStart)
			'b' -> DigitFlags.F_BIN
			'x' -> DigitFlags.F_HEX
			'.' -> {
				dotFound = true
				DigitFlags.F_DEC
			}
			'E', 'e' -> {
				expFound_i = i
				DigitFlags.F_DEC
			}
			'o' -> throw E_JsOctalPrefixNotSupported(from, input, errorAt = checkStart)
			else -> if (c.isDigit()) {
				throw E_JsOctalLegacyNotSupported(from, input, errorAt = checkStart)
			} else if (i < end - 1) {
				throw E_UnsupportedJsNumPrefix(from, input, errorAt = checkStart)
			} else {
				throw E_UnsupportedJsNumSuffix(from, input, errorAt = checkStart)
			}
		}
		if (++i >= end) throw E_JsNumIncomplete(from, input, errorAt = checkStart)
		prev_c = c
		c = input[i]
	} else {
		flag = DigitFlags.F_DEC
	}

	val m = DigitFlags.MAP
	val m_n = m.size
	do {
		val m_i = c.code - DigitFlags.MAP_START
		if (m_i < 0 || m_i >= m_n) throw E_JsNumAssertionFailed(from, input, errorAt = i)
		if (m[m_i] and flag == ZERO_BYTE) run {
			when (c) {
				'_' -> when (prev_c) {
					'_' -> throw E_MultiUnderscoreForbiddenAsJsNumSep(from, input, errorAt = i - 1)
					'b', 'x', '.', 'E', 'e', '+', '-' -> throw E_JsNumSepForbiddenAtStartOrEnd(from, input, errorAt = i)
					else -> return@run
				}
				'.' -> if (!dotFound && flag == DigitFlags.F_DEC) {
					dotFound = true
					return@run
				}
				'E', 'e' -> if (expFound_i == 0 && flag == DigitFlags.F_DEC && i != 0) {
					expFound_i = i
					return@run
				}
				'+', '-' -> if (expFound_i != 0 && expFound_i + 1 == i) {
					return@run
				}
			}
			if (i < end - 1) throw E_UnsupportedDigitInJsNum(from, input, errorAt = i)
			else throw E_UnsupportedJsNumSuffix(from, input, errorAt = checkStart)
		}
		if (++i >= end) break
		prev_c = c
		c = input[i]
	} while (true)

	when (c) {
		// NOTE: Can't use `i` (current index) here since we've already incremented it.
		'_' -> throw E_JsNumSepForbiddenAtStartOrEnd(from, input, errorAt = checkStart)
		'.', 'E', 'e', '+', '-' -> throw E_JsNumIncomplete(from, input, errorAt = checkStart)
	}
}

private fun E_JsNumAssertionFailed(change: FileChange, loadedContents: String, errorAt: Int) = AssertionError(
	E_msg_Js(change, loadedContents, errorAt) + " Assertion failed"
)

private fun E_JsNumSepForbiddenAfterLeadingZero(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Numeric separator cannot be used after leading 0"
)

private fun E_JsOctalPrefixNotSupported(change: FileChange, loadedContents: String, errorAt: Int) = E_JsOctalLegacyNotSupported(change, loadedContents, errorAt)

private fun E_JsOctalLegacyNotSupported(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Unsupported prefix for numeric literal (octal literals are not supported)"
)

private fun E_UnsupportedJsNumPrefix(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Unsupported prefix for numeric literal"
)

private fun E_UnsupportedJsNumSuffix(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Unsupported suffix for numeric literal"
)

private fun E_JsNumIncomplete(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Incomplete numeric literal"
)

private fun E_MultiUnderscoreForbiddenAsJsNumSep(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Only one underscore is allowed as numeric separator"
)

private fun E_JsNumSepForbiddenAtStartOrEnd(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Numeric separators are not allowed at the start or end of numeric literals"
)

private fun E_UnsupportedDigitInJsNum(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Unsupported digit in numeric literal"
)

private fun E_UnsupportedJsNum(change: FileChange, loadedContents: String, errorAt: Int) = InvalidUserDataException(
	E_msg_Js(change, loadedContents, errorAt) + " Unsupported numeric literal"
)
