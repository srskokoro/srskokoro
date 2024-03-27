package build.kt.jvm.app.packaged

import build.api.file.path
import build.api.process.ExecArgs
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
	internal val jdkPath: Provider<String> = jdkHome.map { it.path }

	fun jdkPath(path: String): String = jdkHome.file(path).path

	fun jdepsPath() = jdkPath("bin/jdeps")

	fun jpackagePath() = jdkPath("bin/jpackage")

	// --

	/**
	 * Provides a way to inspect the arguments used after execution; exposed
	 * only for debugging purposes.
	 *
	 * @see org.apache.tools.ant.types.Commandline.toString
	 */
	@get:Internal
	lateinit var jpackageExecArgs: ExecArgs

	@get:Internal
	internal lateinit var jpackageExecArgs_name: String

	protected open fun initJPackageExecArgs() {
		jpackageExecArgs = ExecArgs {
			args(freeArgs.get())

			val spec = spec
			args("-n", spec.appTitle.get().also {
				jpackageExecArgs_name = it
			})

			spec.packageVersionCode.orNull?.let {
				args("--app-version", it)
			}
			spec.description.orNull?.let {
				args("--description", it)
			}
			spec.vendor.orNull?.let {
				args("--vendor", it)
			}
			spec.copyright.orNull?.let {
				args("--copyright", it)
			}
		}
	}
}
