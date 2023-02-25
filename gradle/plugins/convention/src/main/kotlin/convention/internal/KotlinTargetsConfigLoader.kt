package convention.internal

import convention.*
import convention.internal.setup.*
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.add
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
		val kotlinExtensions = (kotlin as ExtensionAware).extensions

		// Will throw if our assumption (that it's an extension) is incorrect.
		val sourceSets = getKotlinSourceSets(kotlinExtensions)

		val targets = kotlin.targets
		val targetsExtension = (targets as ExtensionAware).extensions
		// The following makes sure that the accessors for extensions added to
		// `targets` are generated. See also, "Understanding when type-safe
		// model accessors are available | Gradle Kotlin DSL Primer | 7.5.1" --
		// https://docs.gradle.org/7.5.1/userguide/kotlin_dsl.html#kotdsl:accessor_applicability
		//
		// NOTE: If one day, this caused an exception due to "targets" already
		// existing, simply remove the following. It's likely that it's already
		// implemented for us, and if so, we shouldn't need to do anything.
		kotlinExtensions.add<NamedDomainObjectCollection<KotlinTarget>>("targets", targets)

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
							kotlin.loadTarget(entry, targetsExtension)
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

	private fun getErrorMessageWithConfigFileInfo(message: String) =
		"$message${System.lineSeparator()}- Config file: $configFile"

	private fun errorUnexpectedLine(lineIndex: Int, lineValue: String, cause: Throwable? = null): Nothing {
		throw IllegalStateException(
			getErrorMessageWithConfigFileInfo(
				"Unexpected at line ${lineIndex + 1}: $lineValue"
			), cause
		)
	}

	private fun errorUnknownKotlinTarget(preset: String, cause: Throwable? = null): Nothing {
		throw IllegalStateException(
			getErrorMessageWithConfigFileInfo(
				"Unknown kotlin target: $preset"
			), cause
		)
	}

	private fun KotlinMultiplatformExtension.loadTarget(entry: String, targetsExtension: ExtensionContainer) {
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
			add(::android, name, targetsExtension)

			/* */"jvm" ->
			add(::jvm, name, targetsExtension)

			/* */"js" ->
			add(::js, name, targetsExtension)

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
				}

				run {
					@Suppress("UNCHECKED_CAST")
					targetType as Class<Any> // Hack!
				}.let {
					targetsExtension.add(it, name, target)
				}

				println("Kotlin target preset resolved via reflection: $preset")
				println("- Performance can be improved if reflection wasn't used.")
			}
		}
	}

	private inline fun <reified T : KotlinTarget> add(
		targetProducer: (name: String) -> T, name: String,
		targetsExtension: ExtensionContainer,
	): Unit = targetsExtension.add<T>(name, targetProducer(name))
}
