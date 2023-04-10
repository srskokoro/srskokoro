package conv.deps.serialization

import conv.deps.*
import conv.deps.internal.common.from
import conv.deps.internal.common.removeLast
import conv.deps.internal.common.until
import conv.deps.spec.DependencyBundleSpec
import conv.deps.spec.DependencyBundlesSpec
import conv.deps.spec.DependencyVersionsSpec
import org.gradle.api.InvalidUserDataException
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.Writer

private const val HEADER_INDICATOR = '[' // Must not be a valid JVM identifier character
private const val KEY_SEP = '='
private const val KEY_SEP_len = "$KEY_SEP".length
private const val SUB_PROP_INDICATOR = '\t'
private const val SUB_PROP_INDICATOR_len = "$SUB_PROP_INDICATOR".length

private const val HEADER_00_JVM = "[jvm]"
private const val HEADER_01_PLUGINS = "[plugins]"
private const val HEADER_02_MODULES = "[modules]"
private const val HEADER_03_BUNDLES = "[bundles]"
private const val HEADER_04_INCLUDES = "[includes]"

private const val PREFIX_JVM_00_VER = "ver$KEY_SEP"
private const val PREFIX_JVM_00_VER_len = PREFIX_JVM_00_VER.length

private const val PREFIX_JVM_01_VENDOR = "vendor$KEY_SEP"
private const val PREFIX_JVM_01_VENDOR_len = PREFIX_JVM_01_VENDOR.length

private const val PREFIX_JVM_02_IMPLEMENTATION = "implementation$KEY_SEP"
private const val PREFIX_JVM_02_IMPLEMENTATION_len = PREFIX_JVM_02_IMPLEMENTATION.length

internal fun DependencyVersionsSpec.load(reader: BufferedReader) {
	val state = ReaderState(3)
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

private class ReaderState(initialCapacity: Int) :
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
		HEADER_03_BUNDLES -> state.addFirst {
			consumeBundleName(state, it)
		}
		HEADER_04_INCLUDES -> state.addFirst(
			DependencyVersionsSpec::consumeInclude
		)
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

private fun DependencyBundlesSpec.consumeBundleName(state: ReaderState, bundleHeader: String): Boolean {
	if (bundleHeader.endsWith(KEY_SEP)) {
		val name = bundleHeader.removeLast(KEY_SEP_len)
		val spec = this.bundle_unsafe(name)
		state.addFirst { spec.consumeBundleElement(it) }
		return true
	}
	return false
}

private fun DependencyBundleSpec.consumeBundleElement(line: String): Boolean {
	if (line.startsWith(SUB_PROP_INDICATOR)) {
		val groupEnd = line.indexOf(':', startIndex = SUB_PROP_INDICATOR_len)
		val group = line.from(SUB_PROP_INDICATOR_len, groupEnd) // Let it throw!

		val nameEnd = line.indexOf(':', startIndex = groupEnd + 1)
		val name: String

		val ver: Version?
		if (nameEnd < 0) {
			name = line.from(groupEnd + 1)
			ver = null
		} else {
			name = line.from(groupEnd + 1, nameEnd)
			ver = Version.of_unsafe(line.from(nameEnd + 1))
		}

		modules.putIfAbsent(ModuleId.of_unsafe(group, name), ver)
		return true
	}
	return false
}

private fun DependencyVersionsSpec.consumeInclude(line: String): Boolean {
	if (!line.startsWith(HEADER_INDICATOR)) {
		val file = File(line)
		if (!file.isAbsolute) {
			throw InvalidUserDataException("Path needs to be absolute.")
		}
		prioritizeForLoad(file)
		return true
	}
	return false
}

// --

@Suppress("NOTHING_TO_INLINE")
internal inline fun cannotStore(str: String) = str.startsWith(HEADER_INDICATOR)

@Suppress("NOTHING_TO_INLINE")
internal inline fun DependencyVersionsSpec.store(writer: BufferedWriter): Int = doStore(this, writer)

private fun doStore(spec: DependencyVersionsSpec, writer: Writer): Int = writer.run {
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
	nl++; appendLine(HEADER_03_BUNDLES)

	spec.bundles.forEach pass@{ (bundleName: String, bundleSpec) ->
		if (cannotStore(bundleName)) return@pass // Skip

		nl++
		append(bundleName)
		appendLine(KEY_SEP)

		bundleSpec.modules.forEach { (moduleId, ver) ->
			nl++; append(SUB_PROP_INDICATOR).appendLine(moduleId.toString(ver))
		}
	}

	// --
	nl++; appendLine()
	nl++; appendLine(HEADER_04_INCLUDES)

	spec.includes.forEach pass@{ includedRoot: String ->
		if (cannotStore(includedRoot)) return@pass // Skip

		nl++; appendLine(includedRoot)
	}

	return nl
}
