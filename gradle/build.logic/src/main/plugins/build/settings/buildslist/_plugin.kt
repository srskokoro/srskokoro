package build.settings.buildslist

import build.api.SettingsPlugin
import build.support.io.safeResolve
import build.support.io.transformFileAtomic
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.File
import java.nio.channels.Channels
import java.util.Properties

private const val GRADLE_PROPERTIES = "gradle.properties"

private const val TYPE_PLUGIN_BUILD_PLUS = "plugin-build+:"
private const val TYPE_PLUGIN_BUILD = "plugin-build:"
private const val TYPE_BUILD = "build:"
private const val TYPE_PROP = "prop:"

private const val SETTINGS_BUILDS_LST = "settings.builds.lst"

class _plugin : SettingsPlugin {

	private data class Entry(
		val buildPath: String,
		val customName: String?,
	)

	override fun Settings.applyPlugin() {
		val gradlePropsPropagationPaths = LinkedHashSet<String>()
		val includedPluginBuilds = LinkedHashSet<Entry>()
		val includedBuilds = LinkedHashSet<Entry>()

		providers.of(BuildsListLinesSource::class.java) {
			parameters.settingsDir.set(settingsDir)
		}.get().forEach { line ->
			var end = line.length
			val customName = run(fun(): String? {
				if (line.endsWith('>')) {
					end = line.lastIndexOf('<')
					if (end >= 0) return line.substring(end + 1, line.length - 1)
				}
				return null
			})

			var gradleProps_i = end - (GRADLE_PROPERTIES.length + 1)
			if (gradleProps_i >= 0) {
				val c = line[gradleProps_i]
				if ((c == '/' || c == '\\') && line.startsWith(GRADLE_PROPERTIES, gradleProps_i + 1)) {
					end = gradleProps_i
				} else {
					gradleProps_i = -1
				}
			}

			val start: Int
			if (line.startsWith(TYPE_PLUGIN_BUILD_PLUS)) {
				start = TYPE_PLUGIN_BUILD_PLUS.length
				val entry = Entry(line.substring(start, end), customName)
				includedPluginBuilds.add(entry)
				includedBuilds.add(entry)
			} else if (line.startsWith(TYPE_PLUGIN_BUILD)) {
				start = TYPE_PLUGIN_BUILD.length
				includedPluginBuilds.add(Entry(line.substring(start, end), customName))
			} else if (line.startsWith(TYPE_BUILD)) {
				start = TYPE_BUILD.length
				includedBuilds.add(Entry(line.substring(start, end), customName))
			} else if (line.startsWith(TYPE_PROP) && gradleProps_i >= 0) {
				start = TYPE_PROP.length
			} else {
				throw InvalidUserDataException("Invalid line in `$SETTINGS_BUILDS_LST`: $line")
			}

			if (gradleProps_i >= 0) {
				gradlePropsPropagationPaths.add(line.substring(start, end + (GRADLE_PROPERTIES.length + 1)))
			}
		}

		propagateGradleProps(settingsDir, gradlePropsPropagationPaths)
		includePluginBuilds(this, includedPluginBuilds)
		includeBuilds(this, includedBuilds)
	}

	private fun propagateGradleProps(settingsDir: File, destinationPaths: LinkedHashSet<String>) {
		val propsFile = File(settingsDir, "gradle.properties")
		val props = Properties().apply { load(propsFile.inputStream()) }
		for (dest in destinationPaths) transformFileAtomic(propsFile, settingsDir.safeResolve(dest)) {
			props.store(Channels.newOutputStream(it), " Auto-generated file. DO NOT EDIT!")
		}
	}

	private fun includePluginBuilds(settings: Settings, rootProjects: LinkedHashSet<Entry>) {
		settings.pluginManagement {
			for ((path, name) in rootProjects) {
				if (name == null) {
					includeBuild(path)
				} else {
					includeBuild(path) {
						this.name = name
					}
				}
			}
		}
	}

	private fun includeBuilds(settings: Settings, rootProjects: LinkedHashSet<Entry>) {
		for ((path, name) in rootProjects) {
			if (name == null) {
				settings.includeBuild(path)
			} else {
				settings.includeBuild(path) {
					this.name = name
				}
			}
		}
	}
}

internal abstract class BuildsListLinesSource : ValueSource<List<String>, BuildsListLinesSource.Parameters> {

	interface Parameters : ValueSourceParameters {
		val settingsDir: DirectoryProperty
	}

	override fun obtain() = mutableListOf<String>().also { out ->
		val settingsDir = parameters.settingsDir.get().asFile
		File(settingsDir, SETTINGS_BUILDS_LST).let { lst ->
			if (!lst.isFile) return@let
			lst.forEachLine {
				if (it.isBlank()) return@forEachLine
				when (it[0]) {
					'!', '#' -> return@forEachLine
				}
				out.add(it.trimEnd())
			}
		}
	}
}
