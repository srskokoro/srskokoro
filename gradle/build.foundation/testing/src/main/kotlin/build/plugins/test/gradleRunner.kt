package build.plugins.test

import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun gradleRunner(projectDir: File, vararg taskNames: String): GradleRunner {
	return GradleRunner.create().apply {
		withProjectDir(projectDir)
		withArguments("--info", "--stacktrace", "--continue", *taskNames)

		withPluginClasspath(System.getProperty("java.class.path")
			.splitToSequence(File.pathSeparatorChar)
			.mapTo(ArrayList()) { File(it) })

		withDebug(true) // Run in-process
		forwardOutput()
	}
}
