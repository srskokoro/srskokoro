package conv.deps.serialization

import conv.deps.JvmSetupImplementation
import conv.deps.JvmSetupVendor
import conv.deps.ModuleId
import conv.deps.PluginId
import conv.deps.Version
import conv.deps.spec.DependencyVersionsSpec
import conv.internal.support.from
import conv.internal.support.until
import org.gradle.api.InvalidUserDataException
import java.io.BufferedReader
import java.io.File
import java.io.Writer
import java.nio.file.Path

private const val HEADER_INDICATOR = '[' // Must not be a valid JVM identifier character
private const val KEY_SEP = '='
private const val KEY_SEP_len = "$KEY_SEP".length
private const val SUB_PROP_INDICATOR = '\t'
private const val SUB_PROP_INDICATOR_len = "$SUB_PROP_INDICATOR".length

private const val HEADER_00_JVM = "[jvm]"
private const val HEADER_01_PLUGINS = "[plugins]"
private const val HEADER_02_MODULES = "[modules]"
private const val HEADER_03_INCLUDES = "[includes]"

private const val PREFIX_JVM_00_VER = "ver$KEY_SEP"
private const val PREFIX_JVM_00_VER_len = PREFIX_JVM_00_VER.length

private const val PREFIX_JVM_01_VENDOR = "vendor$KEY_SEP"
private const val PREFIX_JVM_01_VENDOR_len = PREFIX_JVM_01_VENDOR.length

private const val PREFIX_JVM_02_IMPLEMENTATION = "implementation$KEY_SEP"
private const val PREFIX_JVM_02_IMPLEMENTATION_len = PREFIX_JVM_02_IMPLEMENTATION.length

internal fun DependencyVersionsSpec.load(reader: BufferedReader, targetRoot: File) {
	val state = ReaderState(3, targetRoot)
	state.addFirst { consumeHeader(state, it) }

	var lineNum = 1
	while (true) {
		val line = (reader.readLine() ?: break).trimEnd()
		if (line.isNotEmpty()) inner@ while (true) {
			try {
				val action = state.peekFirst()
				if (action(line)) break@inner
			} catch (ex: Exception) {
				failOnUnexpectedLine(lineNum, line, ex)
			}
			state.pollFirst()
		}
		lineNum++
	}
}

private typealias ReaderAction = DependencyVersionsSpec.(line: String) -> Boolean

private class ReaderState(initialCapacity: Int, @JvmField val targetRoot: File) :
	java.util.ArrayDeque<ReaderAction>(initialCapacity)

private fun failOnUnexpectedLine(lineNum: Int, line: String, cause: Throwable): Nothing =
	throw InvalidUserDataException("Unexpected at line $lineNum: $line", cause)

// --

private fun consumeHeader(state: ReaderState, header: String): Boolean {
	when (header) {
		HEADER_00_JVM -> state.addFirst(
			DependencyVersionsSpec::consumeInJvm
		)
		HEADER_01_PLUGINS -> state.addFirst(
			DependencyVersionsSpec::consumePlugin
		)
		HEADER_02_MODULES -> state.addFirst(
			DependencyVersionsSpec::consumeModule
		)
		HEADER_03_INCLUDES -> state.addFirst {
			consumeInclude(state, it)
		}
		else -> return false
	}
	return true
}

private fun DependencyVersionsSpec.consumeInJvm(line: String): Boolean = with(jvm) {
	when {
		line.startsWith(PREFIX_JVM_00_VER) -> if (ver == 0) {
			ver = line.from(PREFIX_JVM_00_VER_len).toInt()
		}
		line.startsWith(PREFIX_JVM_01_VENDOR) -> if (vendor == null) {
			vendor = line.from(PREFIX_JVM_01_VENDOR_len)
				.takeIf { it.isNotEmpty() }
				?.let { JvmSetupVendor.parse(it) }
		}
		line.startsWith(PREFIX_JVM_02_IMPLEMENTATION) -> if (implementation == null) {
			implementation = line.from(PREFIX_JVM_02_IMPLEMENTATION_len)
				.takeIf { it.isNotEmpty() }
				?.let { JvmSetupImplementation.parse(it) }
		}
		else -> return false
	}
	return true
}

private fun DependencyVersionsSpec.consumePlugin(line: String): Boolean {
	if (!line.startsWith(HEADER_INDICATOR)) {
		val idEnd = line.indexOf(':')

		val id = line.until(idEnd) // Let it throw!
		val ver = line.from(idEnd + 1)

		plugins.putIfAbsent(PluginId.of_unsafe(id), Version.of_unsafe(ver))
		return true
	}
	return false
}

private fun DependencyVersionsSpec.consumeModule(line: String): Boolean {
	if (!line.startsWith(HEADER_INDICATOR)) {
		val groupEnd = line.indexOf(':')
		val nameEnd = line.indexOf(':', groupEnd + 1)

		val group = line.until(groupEnd) // Let it throw!
		val name = line.from(groupEnd + 1, nameEnd) // Let it throw!
		val ver = line.from(nameEnd + 1)

		modules.putIfAbsent(ModuleId.of_unsafe(group, name), Version.of_unsafe(ver))
		return true
	}
	return false
}

private fun DependencyVersionsSpec.consumeInclude(state: ReaderState, line: String): Boolean {
	if (!line.startsWith(HEADER_INDICATOR)) {
		// NOTE: The following is undefined if `child` if absolute.
		prioritizeForLoad(File(state.targetRoot, /* child = */ line))
		return true
	}
	return false
}

// --

@Suppress("NOTHING_TO_INLINE")
internal inline fun cannotStore(str: String) = str.startsWith(HEADER_INDICATOR)

@Suppress("NOTHING_TO_INLINE")
@JvmName("-store")
internal inline fun DependencyVersionsSpec.store(writer: Writer): Int = store(this, writer)

private fun store(spec: DependencyVersionsSpec, writer: Writer): Int = writer.run {
	// NOTE: Don't use `BufferedWriter` here, so as to not accidentally call `BufferedWriter.newLine()`

	// The expected number of newline characters in the output
	var nl = 0

	// --
	nl++; appendLine()
	nl++; appendLine(HEADER_00_JVM)

	spec.jvm.apply {
		ver.takeIf { it != 0 }?.let {
			nl++; append(PREFIX_JVM_00_VER).appendLine(it.toString())
		}
		vendor?.let {
			nl++; append(PREFIX_JVM_01_VENDOR).appendLine(it.id)
		}
		implementation?.let {
			nl++; append(PREFIX_JVM_02_IMPLEMENTATION).appendLine(it.id)
		}
	}

	// --
	nl++; appendLine()
	nl++; appendLine(HEADER_01_PLUGINS)

	spec.plugins.forEach { (pluginId, ver) ->
		nl++
		append(pluginId.toString())
		append(':')
		appendLine(ver.toString())
	}

	// --
	nl++; appendLine()
	nl++; appendLine(HEADER_02_MODULES)

	spec.modules.forEach { (moduleId, ver) ->
		nl++
		append(moduleId.toString())
		append(':')
		appendLine(ver.toString())
	}

	// --
	nl++; appendLine()
	nl++; appendLine(HEADER_03_INCLUDES)

	val settingsDir = spec.settings.settingsDir.toPath()
	spec.includes.forEach pass@{
		val includedRoot = settingsDir
			.relativize(Path.of(it))
			.toString()

		if (cannotStore(includedRoot)) return@pass // Skip

		nl++; appendLine(includedRoot)
	}

	return nl
}
