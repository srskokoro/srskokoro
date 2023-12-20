package build.dependencies

import build.support.getFileUri
import build.support.io.UnsafeCharArrayWriter
import build.support.io.safeResolve
import build.support.io.transformFileAtomic
import dependencySettings__name
import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.initialization.ConfigurableIncludedBuild
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.provider.sources.FileBytesValueSource
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ProviderFactory
import org.gradle.initialization.SettingsLocation
import org.gradle.kotlin.dsl.support.serviceOf
import setUpDeps
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Path
import java.util.LinkedList
import java.util.Properties

abstract class DependencySettings internal constructor(val settings: Settings) : ExtensionAware {

	enum class ExportMode {
		EXPORT,
		EXPORT_ONLY,
	}

	var exportMode: ExportMode? = null

	fun export() {
		exportMode = ExportMode.EXPORT
	}

	fun exportOnly() {
		exportMode = ExportMode.EXPORT_ONLY
	}

	// --

	class Props : LinkedHashMap<String, String>()

	val props = Props()
	val plugins = LinkedHashMap<PluginId, String>()
	val modules = LinkedHashMap<ModuleId, String>()

	fun Props.load(propertiesFile: Any?) {
		val s = settings
		val resolved = s.resolve(propertiesFile) // May throw
		s.serviceOf<ProviderFactory>().of(FileBytesValueSource::class.java) {
			parameters.file.set(resolved)
		}.get().let {
			Properties().apply {
				load(ByteArrayInputStream(it))
			}
		}.forEach { (k, v) ->
			put(k as String, v as String)
		}
	}

	val includedBuilds: Iterable<String> get() = includedBuildsDeque

	fun includeBuild(rootProject: Any) {
		val s = settings
		val resolved = s.resolve(rootProject) // May throw
		s.includeBuild(resolved)
		prioritizeForLoad(resolved)
	}

	fun includeBuild(rootProject: Any, configuration: Action<ConfigurableIncludedBuild>) {
		val s = settings
		val resolved = s.resolve(rootProject) // May throw
		s.includeBuild(resolved, configuration)
		prioritizeForLoad(resolved)
	}

	// --

	internal val includedBuildsDeque = LinkedList<String>()

	/**
	 * See the NOTE in the loading logic to understand why this method is named
	 * like this.
	 */
	internal fun prioritizeForLoad(rootProject: File) {
		includedBuildsDeque.addLast(rootProject.canonicalPath)
	}

	// --

	companion object {
		internal const val EXPORT_PATH = ".gradle/deps.dat"

		private fun Settings.resolve(path: Any?): File {
			val unresolved: File = when (path) {
				is String -> File(path)
				is File -> path
				is Path -> path.toFile()
				is FileSystemLocation -> {
					// NOTE: Already absolute (according to API docs).
					return path.asFile
				}
				else -> throw IllegalArgumentException("Cannot convert to File: $path")
			}
			return settingsDir.safeResolve(unresolved)
		}
	}

	private object Logger {
		val logger: org.gradle.api.logging.Logger =
			Logging.getLogger(DependencySettings::class.java)
	}

	internal fun cleanUpExport() {
		File(settings.settingsDir, EXPORT_PATH)
			.delete()
	}

	internal fun setUpForExport() {
		val s = settings
		val settingsFile = s.serviceOf<SettingsLocation>().settingsFile!!
		val targetFile = File(s.settingsDir, EXPORT_PATH)

		DepsCoderInvalidate.runOn(targetFile, s.serviceOf<ProviderFactory>())

		transformFileAtomic(settingsFile, targetFile) { fc ->
			val out = UnsafeCharArrayWriter(DEFAULT_BUFFER_SIZE)

			DepsEncoder(this@DependencySettings, out)
				.encodeFully()

			val cb = out.getUnsafeCharBuffer()
			val bb = DepsCoder_charset.encode(cb)
			fc.write(bb)
		}.let {
			val m = if (it) "Generated new dependency settings export: {}"
			else "Preserved likely up-to-date dependency settings export: {}"
			Logger.logger.info(m, targetFile)
		}
	}

	internal fun setUpForUsageInProjects() {
		hookCustomDependencyResolution(settings, plugins)

		settings.gradle.projectsLoaded(fun(gradle: Gradle) {
			val rootProject = gradle.rootProject

			val logger = Logger.logger
			logger.info("Loading dependency settings for {}", rootProject)

			val dirProvider = rootProject.layout.projectDirectory
			val providers = rootProject.providers

			val loadDeque = includedBuildsDeque
			val loadedSet = HashSet<String>()

			// NOTE: Loads each include in reverse order -- the actual loading
			// logic should make sure that later loads does not override any
			// data contributed by earlier loads.
			//
			// The end result is that later includes seem to always override
			// earlier includes, when in actually, the later includes were
			// simply loaded first, with earlier includes prevented from
			// overriding them upon being loaded next.
			//
			// As a side effect, an include that was included twice would appear
			// as if the second instance of its inclusion overrides any previous
			// includes, including those that included that include first. Thus,
			// guaranteeing that re-including something will always (appear to)
			// override previous includes, just as the user expects.
			while (true) {
				val includedRoot = loadDeque.pollLast() ?: break
				if (loadedSet.add(includedRoot)) {
					val targetDir = dirProvider.dir(includedRoot)
					val targetFile = targetDir.file(EXPORT_PATH)

					val data = (providers.fileContents(targetFile).asBytes.orNull
						?: throw E_DependencySettingsNotFound(targetDir.asFile))
						.toString(DepsCoder_charset)

					DepsDecoder(this@DependencySettings, data, targetDir.asFile)
						.decodeFully()

					logger.info("Loaded dependency settings from file: {}", targetFile)
				}
			}

			logger.info("Loaded dependency settings for {}", rootProject)

			val modules = modules
			val deps = Deps(
				DepsProps(props),
				DepsVersions(plugins = plugins, modules)
			)
			setUpDeps(rootProject, deps)

			gradle.allprojects(fun(project: Project) {
				hookCustomDependencyResolution(project, modules)
			})
		})
	}
}

private fun E_DependencySettingsNotFound(rootProject: File): InvalidUserDataException {
	val rootProjectUri = getFileUri(rootProject)
	return InvalidUserDataException(
		if (
			File(rootProject, "settings.gradle.kts").exists() ||
			File(rootProject, "settings.gradle").exists()
		) {
			"""
			The specified root project did not export its dependency settings.
			- Root project: $rootProjectUri
			- Please call `${DependencySettings::export.name}()` or `${DependencySettings::exportOnly.name}()` in the `$dependencySettings__name` block of
			that project's settings file.
			""".trimIndent()
		} else {
			"""
			The specified root project does not have a `settings.gradle` file (and is thus
			unlikely to contribute a dependency settings).
			- Root project: $rootProjectUri
			""".trimIndent()
		}
	)
}
