package conv.internal

import conv.internal.setup.*
import conv.util.*
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.nio.file.Files

var Project.skipPlaceholderGenerationForKotlinTargetsConfigLoader: Boolean
	get() = extra.let {
		it.has(::skipPlaceholderGenerationForKotlinTargetsConfigLoader.name) &&
			it[::skipPlaceholderGenerationForKotlinTargetsConfigLoader.name] as Boolean
	}
	set(value) = extra.set(::skipPlaceholderGenerationForKotlinTargetsConfigLoader.name, value)

internal class KotlinTargetsConfigLoader(
	private val providers: ProviderFactory,
	private val config: RegularFile,
) {
	private val configFile get() = config.asFile

	companion object {
		private const val MODE_TOP_LEVEL = 0
		private const val MODE_TARGETS = 1
		private const val MODE_SOURCE_SETS = 2

		private const val SECTION_HEADER_TARGETS = "targets:"
		private const val SECTION_HEADER_SOURCE_SETS = "customSourceSets:"

		private const val LIST_START = "- "
		private const val LIST_START_LEN = LIST_START.length
	}

	fun loadInto(kotlin: KotlinMultiplatformExtension, skipPlaceholderGeneration: Boolean) {
		configFile.takeIf { !it.isFile || it.length() <= 2L }?.let {
			val deleted = it.delete()
			if (skipPlaceholderGeneration && (deleted || !it.exists())) {
				// Don't generate placeholder (but do let Gradle listen for
				// changes to the file once it actually exists).
				return@let // Should skip only placeholder generation logic
			}
			try {
				Files.createFile(it.toPath()) // Let it throw!
			} catch (ex: IOException) {
				// Wrap it so that the exception class name also gets printed
				// (and not just the exception message).
				throw InvalidUserDataException(ex.toString(), ex)
			}
			it.writeText(
				"""
				$SECTION_HEADER_TARGETS
				# Example entry:
				#$LIST_START<target>[:<name>]

				$SECTION_HEADER_SOURCE_SETS
				# Example entry:
				#$LIST_START<name>

				""".trimIndent()
			)
		}

		val configBytes = providers.fileContents(config)
			.asBytes
			.orNull
			?: return // Skip loading otherwise

		val configChars = String(configBytes, Charsets.UTF_8)
		doParse(configChars, kotlin)
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
					SECTION_HEADER_TARGETS -> {
						mode = MODE_TARGETS
						return@pass
					}
					SECTION_HEADER_SOURCE_SETS -> {
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
			/* */"android",
			/* */"androidTarget" ->
			/*  */androidTarget(name) asExtensionIn targetsExtensions

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
