package conv.redwood.ui.wv.setup

import conv.redwood.ui.wv.setup.WvSetupBuilder.Companion.HEAD_NAME
import conv.redwood.ui.wv.setup.WvSetupBuilder.Companion.TAIL_NAME
import conv.redwood.ui.wv.setup.WvSetupBuilder.Companion.TYPE_s
import conv.redwood.ui.wv.setup.WvSetupBuilder.SetupEntry
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.Directory
import org.gradle.api.file.EmptyFileVisitor
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.RelativePath
import java.io.File
import java.util.Arrays

internal class WvSetupBuilder(
	isDebugBuild: Boolean,
	schemaPackage: String,
	classpathUnion: FileTree,
) : EmptyFileVisitor() {
	companion object {
		const val OBJECT_NAME = "WvSetup"
		const val JS_BASE_NAME = OBJECT_NAME

		const val HEAD_NAME = "head"
		const val HEAD_NAME_len = HEAD_NAME.length

		const val TAIL_NAME = "tail"
		const val TAIL_NAME_len = TAIL_NAME.length

		const val TYPE_t = 't'
		const val TYPE_m = 'm'
		const val TYPE_s = '$'
	}

	private val schemaPackageSegments = schemaPackage.split('.')

	private val setupPrologs = ArrayList<SetupEntry>()
	private val setupBodies = ArrayList<SetupEntry>()
	private val setupEpilogs = ArrayList<SetupEntry>()

	abstract class SetupEntry(
		@JvmField val id: Int, @JvmField val type: Char,
		@JvmField val file: File,
	) : Comparable<SetupEntry> {

		fun entryEquals(other: SetupEntry) =
			type == other.type && id == other.id

		final override fun compareTo(other: SetupEntry): Int {
			var cmp = type.compareTo(other.type)
			if (cmp == 0) cmp = id.compareTo(other.id)
			return cmp
		}

		open fun appendToKtSetup(sb: StringBuilder): Unit =
			throw UnsupportedOperationException()

		abstract fun appendToJsSetup(sb: StringBuilder)
	}

	val ktSetup: String
	val jsSetup: String

	init {
		try {
			classpathUnion.visit(this)
		} catch (ex: GradleException) {
			// Need to do this. Otherwise, build failures won't be meaningful
			// unless the build was run with the `--stacktrace` option.
			if (ex.javaClass == GradleException::class.java) ex.cause?.let { cause ->
				val origin = cause.stackTrace.firstOrNull() ?: return@let
				if (origin.className.startsWith("${WvSetupBuilder::class.java.packageName}.")) {
					throw cause
				}
			}
			throw ex
		}

		val kt = StringBuilder()
		if (schemaPackage.isNotEmpty()) {
			kt.append("package ")
			kt.append(schemaPackage)
		}
		kt.append("\n\npublic object $OBJECT_NAME {\n")

		val js = StringBuilder("'use strict';(() => {\n")
		js.append("const IS_DEBUG_BUILD =")
		js.append(if (isDebugBuild) "true\n" else "false\n")

		processWvSetupEntries(setupPrologs) {
			it.appendToJsSetup(js)
		}
		processWvSetupEntries(setupBodies) {
			it.appendToKtSetup(kt)
			it.appendToJsSetup(js)
		}
		processWvSetupEntries(setupEpilogs) {
			it.appendToJsSetup(js)
		}

		js.append("})()\n")
		kt.append("}\n")

		this.ktSetup = kt.toString()
		this.jsSetup = js.toString()
	}

	override fun visitFile(visit: FileVisitDetails) {
		val name = visit.name

		// Must match format `?.*.js` (and not `?.js`)
		if (name.length < 6 || !name.endsWith(".js", ignoreCase = true) || name[1] != '.') return

		// Must be in the expected "package" directory
		val relativePath = visit.relativePath
		relativePath.segments.let { segments ->
			val segments_size = segments.size
			val targetSegments = schemaPackageSegments
			for (i in targetSegments.indices)
				if (i >= segments_size || segments[i] != targetSegments[i])
					return
		}

		val idLeadDotIdx = name.lastIndexOf('.', name.length - 4) // Excludes the ".js" file extension
		val id = run<Int> {
			if (idLeadDotIdx >= 0) {
				val digits = name.substring(idLeadDotIdx + 1, name.length - 3) // Excludes the ".js" file extension
				try {
					return@run digits.toInt()
				} catch (_: NumberFormatException) {
					// Fall through
				}
			}
			throw E_MissingJsSetupId(name)
		}

		val file = visit.file
		val type = name[0]
		val intro = when (type) {
			TYPE_t -> "t$("
			TYPE_m -> "m$("
			TYPE_s -> {
				if (name.regionMatches(2, "$HEAD_NAME.", 0, length = HEAD_NAME_len + 1, ignoreCase = true)) {
					setupPrologs
				} else if (name.regionMatches(2, "$TAIL_NAME.", 0, length = TAIL_NAME_len + 1, ignoreCase = true)) {
					setupEpilogs
				} else {
					throw E_NonHeadNorTailJsSetup(name)
				}.add(object : SetupEntry(id, type, file) {
					override fun appendToJsSetup(sb: StringBuilder) {
						appendJsSetupContentHeaderLine(sb, relativePath)
						appendJsSetupContent(sb, this.file)
						sb.appendLine()
					}
				})
				return // Done
			}
			else -> throw E_UnsupportedJsSetupNameInitial(name)
		}

		val ktEntryName = name
			.substring(0, idLeadDotIdx)
			.replace('.', '_')

		setupBodies += object : SetupEntry(id, type, file) {

			override fun appendToKtSetup(sb: StringBuilder) {
				sb.append("\tpublic const val ")
				sb.append(ktEntryName)
				sb.append(" = ")
				appendWvSetupId_quoted(sb, this.id)
				sb.appendLine()
			}

			override fun appendToJsSetup(sb: StringBuilder) {
				appendJsSetupContentHeaderLine(sb, relativePath)
				sb.append(intro)
				appendWvSetupId_quoted(sb, this.id)
				sb.append(", ")
				appendJsSetupContent(sb, this.file)
				sb.append(")\n")
			}
		}
	}

	fun resolveOutputFile(baseDir: Directory, filename: String): File {
		val file = File(baseDir.asFile, buildString {
			for (segment in schemaPackageSegments) {
				append(segment)
				append(File.separator)
			}
			append(filename)
		})
		file.parentFile.mkdirs()
		return file
	}
}

private fun E_UnsupportedJsSetupNameInitial(filename: String) = InvalidUserDataException("Invalid JS setup filename. Unsupported initial character: $filename")
private fun E_NonHeadNorTailJsSetup(filename: String) = InvalidUserDataException("Invalid JS setup filename. Expected \"$TYPE_s.$HEAD_NAME.*.js\" or \"$TYPE_s.$TAIL_NAME.*.js\" but got instead name: $filename")
private fun E_MissingJsSetupId(filename: String) = InvalidUserDataException("Invalid JS setup filename. Digits expected right before \".js\" but got instead name: $filename")
private fun E_DuplicateSetupId(first: File, second: File) = InvalidUserDataException("Duplicate setup IDs found for the following files:\n- $first\n- $second")

private fun appendJsSetupContentHeaderLine(sb: StringBuilder, relativePath: RelativePath) {
	sb.append(";// Source: ")
	sb.append(relativePath.pathString)
	sb.appendLine()
}

private fun appendJsSetupContent(sb: StringBuilder, file: File) {
	file.reader().use {
		val buffer = CharArray(DEFAULT_BUFFER_SIZE)
		while (true) {
			val n = it.read(buffer)
			if (n > 0) {
				sb.appendRange(buffer, 0, n)
			} else break
		}
	}
	var i = sb.length - 1
	while (i >= 0 && sb[i].isWhitespace()) i--
	sb.delete(i + 1, sb.length)
}

private fun processWvSetupEntries(entries: ArrayList<SetupEntry>, action: (SetupEntry) -> Unit) {
	if (entries.isEmpty()) return

	val array = entries.toTypedArray()
	Arrays.sort(array)

	val n = array.size
	var i = 0
	var setup = array[i]

	while (true) {
		action(setup)
		if (++i >= n) break
		val next = array[i]
		if (next.entryEquals(setup)) {
			throw E_DuplicateSetupId(setup.file, next.file)
		}
		setup = next
	}
}
