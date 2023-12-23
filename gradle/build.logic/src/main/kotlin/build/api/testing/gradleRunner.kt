package build.api.testing

import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun gradleRunner(projectDir: File, vararg taskNames: String): GradleRunner {
	return GradleRunner.create().apply {
		withProjectDir(projectDir)
		withArguments("--info", "--stacktrace", "--continue", *taskNames)

		System.getProperty(TestSystemProps.CLASSPATH).let(fun(s) {
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
