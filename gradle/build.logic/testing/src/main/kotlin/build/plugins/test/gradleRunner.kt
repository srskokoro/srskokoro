package build.plugins.test

import org.gradle.testkit.runner.GradleRunner
import java.io.File

internal const val gradleRunner_CLASSPATH_SYS_PROP = "build.plugins.test.classpath"

fun gradleRunner(projectDir: File, vararg taskNames: String): GradleRunner {
	return GradleRunner.create().apply {
		withProjectDir(projectDir)
		withArguments("--info", "--stacktrace", "--continue", *taskNames)

		System.getProperty(gradleRunner_CLASSPATH_SYS_PROP).let(fun(s) {
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
