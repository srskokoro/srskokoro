package build.kt.jvm.app.packaged

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class JPackageBaseTask : PackagedSpecBaseTask() {

	/**
	 * The list of free arguments passed directly to the `jpackage` command.
	 *
	 * They can be used in addition to the arguments that are provided by
	 * dedicated options.
	 */
	@get:Input
	abstract val freeArgs: ListProperty<String>

	// --

	@get:Internal
	abstract val jdkHome: DirectoryProperty

	@Suppress("unused", "LeakingThis")
	@get:Input
	internal val jdkPath: Provider<String> = jdkHome.map { it.asFile.path }

	fun jdkPath(path: String): String = jdkHome.file(path).get().asFile.path

	fun jdepsPath() = jdkPath("bin/jdeps")

	fun jpackagePath() = jdkPath("bin/jpackage")
}
