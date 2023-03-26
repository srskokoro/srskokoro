package conv.internal

import conv.internal.setup.*
import conv.util.*
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.lang.reflect.InvocationTargetException

internal class KotlinTargetsConfigLoader(
	private val providers: ProviderFactory,
	private val config: RegularFile,
) {
	private val configFile get() = config.asFile

	companion object {
		private const val MODE_TOP_LEVEL = 0
		private const val MODE_TARGETS = 1
		private const val MODE_SOURCE_SETS = 2

		private const val LIST_START = "- "
		private const val LIST_START_LEN = "- ".length
	}

	fun loadInto(kotlin: KotlinMultiplatformExtension) {
		val configBytes = providers.fileContents(config)
			.asBytes
			.orNull

		if (configBytes != null) {
			val configChars = String(configBytes, Charsets.UTF_8)
			doParse(configChars, kotlin)
		} else {
			println("Not parsing kotlin targets (and source sets) from config file " + configFile.run {
				when {
					exists().not() -> "as it's not present."
					isFile.not() -> "as it's not a file."
					else -> "for unknown reasons."
				}
			})
			println("- Expected config file: $configFile")
		}
	}

	private fun doParse(configFileContents: String, kotlin: KotlinMultiplatformExtension) {
		val targetsExtensions = kotlin.targets.extensions
		val sourceSets = getSourceSets(kotlin)

		var mode = MODE_TOP_LEVEL
		configFileContents.lineSequence().forEachIndexed pass@{ i, raw ->
			if (raw.isEmpty()) return@pass
			if (raw[0] == '#') return@pass
			val cur = raw.trimEnd()
			try {
				when (cur) {
					"" -> return@pass
					"targets:" -> {
						mode = MODE_TARGETS
						return@pass
					}
					"customSourceSets:" -> {
						mode = MODE_SOURCE_SETS
						return@pass
					}
				}
				if (cur.startsWith(LIST_START)) {
					val entry = cur.substring(LIST_START_LEN)
					when (mode) {
						MODE_TARGETS -> {
							kotlin.loadTarget(entry, targetsExtensions)
							return@pass
						}
						MODE_SOURCE_SETS -> {
							sourceSets.register(entry)
							return@pass
						}
					}
				}
				errorUnexpectedLine(i, raw)
			} catch (ex: Exception) {
				errorUnexpectedLine(i, raw, ex)
			}
		}
	}

	// --

	private fun getErrorMessageWithConfigFileInfo(message: String, cause: Throwable?) = buildString {
		appendLine(message)
		appendLine("- Config file: $configFile")
		if (cause != null)
			appendLine("- Exception: $cause")
	}

	private fun errorUnexpectedLine(lineIndex: Int, lineValue: String, cause: Throwable? = null): Nothing {
		throw IllegalStateException(
			getErrorMessageWithConfigFileInfo(
				"Unexpected at line ${lineIndex + 1}: $lineValue",
				cause
			), cause
		)
	}

	private fun errorUnknownKotlinTarget(preset: String, cause: Throwable? = null): Nothing {
		throw IllegalStateException(
			getErrorMessageWithConfigFileInfo(
				"Unknown kotlin target: $preset",
				cause
			), cause
		)
	}

	private fun KotlinMultiplatformExtension.loadTarget(entry: String, targetsExtensions: ExtensionContainer) {
		val preset: String
		val name: String

		// Expected format: <preset>:<name>
		val sepIdx = entry.indexOf(':')
		if (sepIdx >= 0) {
			preset = entry.substring(0, sepIdx)
			name = entry.substring(sepIdx + 1)
		} else {
			preset = entry
			name = entry
		}

		when (preset) {
			/* */"android" ->
			/*  */android(name) asExtensionIn targetsExtensions

			/* */"jvm" ->
			/*  */jvm(name) asExtensionIn targetsExtensions

			/* */"js" ->
			/*  */js(name) asExtensionIn targetsExtensions

			// TODO Add more as necessary to avoid resolving things via reflection

			// Resolve via reflection instead
			else -> {
				val targetMethod = try {
					KotlinMultiplatformExtension::class.java.getMethod(preset, String::class.java)
				} catch (ex: NoSuchMethodException) {
					errorUnknownKotlinTarget(preset, ex)
				}

				val targetType = targetMethod.returnType
				if (!KotlinTarget::class.java.isAssignableFrom(targetType)) {
					errorUnknownKotlinTarget(preset)
				}

				val target = try {
					targetMethod.invoke(this, name)
				} catch (ex: InvocationTargetException) {
					throw ex.targetException
				} as KotlinTarget

				run {
					@Suppress("UNCHECKED_CAST")
					targetType as Class<Any> // Hack!
				}.let {
					targetsExtensions.add(it, target.name, target)
				}

				println("Kotlin target preset resolved via reflection: $preset")
				println("- Performance can be improved if reflection wasn't used.")
			}
		}
	}
}
