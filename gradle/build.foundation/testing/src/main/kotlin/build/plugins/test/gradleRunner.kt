package build.plugins.test

import org.gradle.testkit.runner.GradleRunner
import java.io.File

// NOTE: We use an environment variable and not a system property because Gradle
// passes system properties as command line arguments which can cause issues on
// Windows due to command line length limitation.
internal const val gradleRunner_PLUGIN_CLASSPATH_ENV_VAR = "BUILD_PLUGINS_TEST_CLASSPATH"

fun gradleRunner(projectDir: File, vararg taskNames: String): GradleRunner {
	return GradleRunner.create().apply {
		withProjectDir(projectDir)
		withArguments("--info", "--stacktrace", "--continue", *taskNames)

		System.getenv(gradleRunner_PLUGIN_CLASSPATH_ENV_VAR).let(fun(s) {
			if (s == null) {
				withPluginClasspath()
			} else {
				withPluginClasspath(ArrayList<File>().apply {
					s.split(File.pathSeparator).forEach { add(File(it)) }
				})
			}
		})

		withDebug(true) // Run in-process
		forwardOutput()
	}
}
