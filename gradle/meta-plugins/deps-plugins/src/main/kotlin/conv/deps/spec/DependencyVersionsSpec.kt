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
import org.gradle.api.Project
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.initialization.ConfigurableIncludedBuild
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ProviderFactory
import org.gradle.initialization.SettingsLocation
import org.gradle.kotlin.dsl.create
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import javax.inject.Inject

private const val DEPENDENCY_VERSIONS_EXPORT_PATH = "build/deps.versions.dat"

abstract class DependencyVersionsSpec internal constructor(val settings: Settings) : DependencyBundlesSpec(), ExtensionAware {
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

	/**
	 * See the NOTE in the loading logic to understand why this method is named
	 * like this.
	 */
	internal fun prioritizeForLoad(rootProject: File) {
		includesDeque.addLast(rootProject.canonicalPath)
	}

	@get:Inject internal abstract val providers: ProviderFactory

	internal fun setUpForUseInProjects() {
		hookCustomDependencyResolution(settings, plugins)

		settings.gradle.projectsLoaded(fun(gradle: Gradle) {
			val rootProject = gradle.rootProject
			val dirProvider = rootProject.layout.projectDirectory
			val providers = providers

			val loadDeque = includesDeque
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

			val deps: DependencyVersions = rootProject.extensions.create(extensionName, this)

			gradle.allprojects(fun(project: Project) {
				if (project != rootProject) {
					project.extensions.add(extensionName, deps)
				}
				hookCustomDependencyResolution(project, deps.modules)
			})
		})
	}

	@get:Inject internal abstract val layout: SettingsLocation

	internal fun setUpForExport(): Unit = settings.gradle.settingsEvaluated(fun(settings: Settings) {
		val settingsDir = settings.settingsDir

		val target = File(settingsDir, DEPENDENCY_VERSIONS_EXPORT_PATH)
		val targetPath = target.toPath()

		if (target.isFile) run<Unit> {
			val settingsFile = layout.settingsFile ?: return@run

			val targetAttr = Files.readAttributes(targetPath, BasicFileAttributes::class.java)
			val targetModMs = targetAttr.lastModifiedTime().toMillis()

			// Check if the target file was generated after the settings file,
			// and that it was not modified since its generation.
			if (targetModMs > settingsFile.lastModified() && targetModMs == targetAttr.creationTime().toMillis()) {
				return@settingsEvaluated // It's likely up-to-date
			}
		}

		val stream = UnsafeByteArrayOutputStream()
		val nl = stream.bufferedWriter().use { writer -> // Using `use` here to auto-flush buffer
			store(writer)
		}

		try {
			// Let the following throw!
			if (!Files.deleteIfExists(targetPath))
				Files.createDirectories(targetPath.parent)

			FileOutputStream(target).use {
				it.write(stream.buffer, 0, stream.size)
			}

			// Set up the file timestamps for our custom up-to-date check
			targetPath.setModTimeAsCreateTime()

			// Check if the user gave invalid data by inserting newlines in
			// module IDs, version strings, etc.
			if (nl != stream.buffer.count { it == '\n'.toByte() }) {
				failOnUnexpectedNewlineCount()
			}
		} catch (ex: Throwable) {
			throw DependencyVersionsFileException.wrapJudiciously(target, ex)
		}
	})
}

private fun Settings.resolveForIncludeBuild(rootProject: Any?): File = when (rootProject) {
	is String -> settingsDir.safeResolve(rootProject)
	is File -> rootProject
	is Path -> rootProject.toFile()
	is FileSystemLocation -> rootProject.asFile
	else -> failOnArgToFile(rootProject)
}

private fun Path.setModTimeAsCreateTime() {
	val attrView = Files.getFileAttributeView(this, BasicFileAttributeView::class.java)

	// Sets the creation time to be the same as the last modification time.
	val lastModifiedTime = attrView.readAttributes().lastModifiedTime()
	attrView.setTimes(null, null, /* createTime = */ lastModifiedTime)

	// Sets the last modification time to be the same as the creation time, that
	// is, if necessary.
	//
	// Needed since the creation time may have less granularity than the last
	// modification time, and so, the former have likely been rounded to the
	// nearest supported value, making it different from the latter. This hack
	// fixes that.
	//
	// ASSUMPTION: Usually, the creation time has a higher granularity than the
	// last modification time, so the following usually won't be needed. See
	// also, https://learn.microsoft.com/en-us/windows/win32/sysinfo/file-times
	//
	val createTime = attrView.readAttributes().creationTime()
	if (createTime != lastModifiedTime) {
		attrView.setTimes(/* lastModifiedTime = */ createTime, null, null)
	}
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
