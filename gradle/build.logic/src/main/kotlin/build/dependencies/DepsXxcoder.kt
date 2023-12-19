package build.dependencies

import build.dependencies.DependencySettings.Companion.EXPORT_PATH
import build.support.io.UnsafeCharArrayWriter
import build.support.lineInfoUriAt
import org.gradle.api.InvalidUserDataException
import java.io.File
import java.nio.file.Path

private const val PC_PROP = 'X'
private const val PC_PLUGIN = 'P'
private const val PC_MODULE = 'M'
private const val PC_INCLUDE = 'I'

internal class DepsEncoder(
	private val deps: DependencySettings,
	private val out: UnsafeCharArrayWriter,
) {
	fun encodeFully() {
		val out = out
		val deps = deps
		deps.props.forEach { (k, v) -> out.encodeEntry(PC_PROP, k, v) }
		deps.plugins.forEach { (id, v) -> out.encodeEntry(PC_PLUGIN, id.toString(), v) }
		deps.modules.forEach { (id, v) -> out.encodeEntry(PC_MODULE, id.toString(), v) }

		/** @see DepsDecoder.sourceSettingsDir */
		val sourceSettingsDir = deps.settings.settingsDir.toPath()
		deps.includedBuildsDeque.forEach {
			/** @see DepsDecoder.decodeInclude */
			val relativePath = sourceSettingsDir.relativize(Path.of(it)).toString()
			out.encodeEntry(PC_INCLUDE, relativePath)
		}
	}

	private fun UnsafeCharArrayWriter.encodeEntry(pc: Char, k: String, v: String) {
		append(pc)
		append(','); formatCell(k)
		append(','); formatCell(v)
		appendLine()
	}

	private fun UnsafeCharArrayWriter.encodeEntry(pc: Char, v: String) {
		append(pc)
		append(','); formatCell(v)
		appendLine()
	}

	/** @see DepsDecoder.nextCell */
	private fun UnsafeCharArrayWriter.formatCell(value: String) {
		for (c in value) {
			/** @see DepsDecoder.nextCell */
			when (c) {
				',' -> 'C'.code
				'\\' -> 'S'.code
				'\n' -> 'n'.code
				'\r' -> 'r'.code
				else -> {
					write(c.code)
					continue
				}
			}.let {
				write('\\'.code)
				write(it)
			}
		}
	}
}

internal class DepsDecoder(
	private val deps: DependencySettings,
	private val data: String,
	private val sourceSettingsDir: File,
) {
	private var i = 0
	private var eol = false

	fun decodeFully() {
		while (i < data.length) {
			when (val c = data[i++]) {
				PC_PROP -> {
					consumeComma(); decodeProp()
				}
				PC_PLUGIN -> {
					consumeComma(); decodePlugin()
				}
				PC_MODULE -> {
					consumeComma(); decodeModule()
				}
				PC_INCLUDE -> {
					consumeComma(); decodeInclude()
				}
				'#', '!' -> {
					consumeLine()
					continue
				}
				'\n', '\r' -> continue
				else -> if (!c.isWhitespace()) throw E_UnexpectedChar()
			}
			consumeBlankLine()
		}
	}

	private fun consumeComma() {
		if (i >= data.length) throw E_UnexpectedEndOfData()
		if (data[i++] != ',') throw E_UnexpectedChar()
	}

	private fun consumeLine() {
		if (eol) {
			eol = false
		} else while (i < data.length) when (data[i++]) {
			'\n', '\r' -> break
		}
	}

	private fun consumeBlankLine() {
		if (eol) {
			eol = false
		} else while (i < data.length) when (val c = data[i++]) {
			'\n', '\r' -> break
			' ', '\t' -> continue
			else -> if (!c.isWhitespace()) throw E_UnexpectedChar()
		}
	}

	private fun decodeProp() {
		val k = nextCell()
		val v = nextCell()
		deps.props.putIfAbsent(k, v)
	}

	private fun decodePlugin() {
		val id = nextCell()
		val v = nextCell()
		deps.plugins.putIfAbsent(PluginId.of_unsafe(id), v)
	}

	private fun decodeModule() {
		val id = nextCell()
		val v = nextCell()
		deps.modules.putIfAbsent(ModuleId.of_unsafe(id), v)
	}

	private fun decodeInclude() {
		val relativePath = nextCell()
		// NOTE: The following is undefined if `child` is absolute.
		deps.prioritizeForLoad(File(sourceSettingsDir, /* child = */ relativePath))
	}

	/** @see DepsEncoder.formatCell */
	private fun nextCell(): String {
		if (eol) return ""
		val out = StringBuilder()
		while (i < data.length) {
			when (val c = data[i++]) {
				'\n', '\r' -> {
					eol = true
					break
				}

				',' -> break

				/** @see DepsEncoder.formatCell */
				'\\' -> if (i < data.length) when (data[i++]) {
					'C' -> out.append(',')
					'S' -> out.append('\\')
					'n' -> out.append('\n')
					'r' -> out.append('\r')
					else -> throw E_UnexpectedChar()
				} else throw E_UnexpectedEndOfData()

				else -> out.append(c)
			}
		}
		return out.toString()
	}

	private fun E_UnexpectedChar() = InvalidUserDataException(
		"Unexpected character at ${getFileUriLineInfoAt(i - 1)}"
	)

	private fun E_UnexpectedEndOfData() = InvalidUserDataException(
		"Unexpected end of data at ${getFileUriLineInfoAt(data.length)}"
	)

	private fun getFileUriLineInfoAt(index: Int) =
		data.lineInfoUriAt(index, File(sourceSettingsDir, EXPORT_PATH))
}
