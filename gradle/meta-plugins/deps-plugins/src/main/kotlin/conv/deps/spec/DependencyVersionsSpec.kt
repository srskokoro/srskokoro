package conv.deps.spec

import conv.deps.*
import conv.deps.internal.DependencyVersionsFileException
import conv.deps.internal.common.UnsafeByteArrayOutputStream
import conv.deps.internal.common.safeResolve
import conv.deps.serialization.load
import conv.deps.serialization.store
import dependencyVersionsSetup__name
import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.initialization.ConfigurableIncludedBuild
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

private const val DEPENDENCY_VERSIONS_EXPORT_PATH = "build/deps.versions.dat"

abstract class DependencyVersionsSpec internal constructor(
	val settings: Settings,
	private val providers: ProviderFactory,
) : DependencyBundlesSpec(), ExtensionAware {
	val jvm: JvmSetupSpec = extensions.create(::jvm.name)

	val plugins: MutableMap<PluginId, Version> = HashMap()
	val modules: MutableMap<ModuleId, Version> = HashMap()

	val includes: Iterable<String> get() = includesDeque

	fun includeBuild(rootProject: Any) {
		val s = settings
		val resolved = s.resolveForIncludeBuild(rootProject)
		s.includeBuild(rootProject)
		prioritizeForLoad(resolved)
	}

	fun includeBuild(rootProject: Any, configuration: Action<ConfigurableIncludedBuild>) {
		val s = settings
		val resolved = s.resolveForIncludeBuild(rootProject)
		s.includeBuild(rootProject, configuration)
		prioritizeForLoad(resolved)
	}

	// --

	internal var extensionName: String = ""

	private val includesDeque = LinkedList<String>()
	private val includesLoaded = HashSet<String>()

	/**
	 * See the NOTE in the loading logic to understand why this method is named
	 * like this.
	 */
	internal fun prioritizeForLoad(rootProject: File) {
		includesDeque.addLast(rootProject.canonicalPath)
	}

	internal fun setUpForUseInProjects() {
		hookCustomDependencyResolution(settings, plugins)

		settings.gradle.projectsLoaded {
			val rootProject = rootProject
			val dirProvider = rootProject.layout.projectDirectory

			val loadDeque = includesDeque
			val loadedSet = includesLoaded

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
					val target = dirProvider.dir(includedRoot)
						.file(DEPENDENCY_VERSIONS_EXPORT_PATH)

					ByteArrayInputStream(
						providers.fileContents(target).asBytes.orNull
						?: failOnDependencyVersionsNotExported(includedRoot)
					).bufferedReader().use { reader -> // Using `use` here because... paranoia
						try {
							load(reader)
						} catch (ex: Throwable) {
							throw DependencyVersionsFileException.wrapJudiciously(target, ex)
						}
					}
				}
			}

			val deps: DependencyVersions = rootProject.extensions
				.create(extensionName, this@DependencyVersionsSpec)

			allprojects {
				if (this != rootProject) extensions.add(extensionName, deps)
				hookCustomDependencyResolution(this, deps.modules)
			}
		}
	}

	internal fun setUpForExport(): Unit = settings.gradle.settingsEvaluated {
		val stream = UnsafeByteArrayOutputStream()
		val nl = stream.bufferedWriter().use { writer -> // Using `use` here to auto-flush buffer
			store(writer)
		}

		val target = File(rootDir, DEPENDENCY_VERSIONS_EXPORT_PATH)
		try {
			if (
				target.isFile &&
				target.length() == stream.size.toLong() &&
				target.readBytes().let { Arrays.equals(it, 0, it.size,/**/ stream.buffer, 0, stream.size) }
			) {
				return@settingsEvaluated // Same contents
			}

			// Let the following throw!
			if (!Files.deleteIfExists(target.toPath()))
				Files.createDirectories(target.parentFile.toPath())

			FileOutputStream(target).use {
				it.write(stream.buffer, 0, stream.size)
			}

			// Check if the user gave invalid data by inserting newlines in module
			// IDs, version strings, etc.
			if (nl != stream.buffer.count { it == '\n'.toByte() }) {
				failOnUnexpectedNewlineCount()
			}
		} catch (ex: Throwable) {
			throw DependencyVersionsFileException.wrapJudiciously(target, ex)
		}
	}
}

private fun Settings.resolveForIncludeBuild(rootProject: Any?): File = when (rootProject) {
	is String -> rootDir.safeResolve(rootProject)
	is File -> rootProject
	is Path -> rootProject.toFile()
	is FileSystemLocation -> rootProject.asFile
	else -> failOnArgToFile(rootProject)
}

private fun failOnDependencyVersionsNotExported(includedRoot: String): Nothing = throw FileNotFoundException(
	"""
	The specified root project did not export a dependency versions file.
	- Root project: $includedRoot
	- Please call `${dependencyVersionsSetup__name}.${DependencyVersionsSetup::export.name}()` in that project's settings file.
	""".trimIndent()
)

private fun failOnUnexpectedNewlineCount(): Nothing = throw InvalidUserDataException(
	"Malformed output caused by newlines in unexpected places. Please make " +
	"sure that module IDs, version strings, etc., does not contain newline " +
	"characters."
)
