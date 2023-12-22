package build.test.io

import org.gradle.testfixtures.ProjectBuilder
import java.io.File

fun projectBuilder() = ProjectBuilder.builder().withGradleUserHomeDir(gradleUserHome)

fun projectBuilder(projectDir: File?) = projectBuilder().withProjectDir(projectDir)

inline fun buildProject(configure: ProjectBuilder.() -> Unit = {}) =
	projectBuilder().apply(configure).build()

inline fun buildProject(projectDir: File?, configure: ProjectBuilder.() -> Unit = {}) =
	projectBuilder(projectDir).apply(configure).build()

// --

val gradleUserHome: File get() = _gradleUserHome.value

private object _gradleUserHome {
	/** @see org.gradle.testkit.runner.GradleRunner.withGradleVersion */
	val value = run(fun(): File {
		var path = System.getProperty("gradle.user.home")
		if (path == null) {
			path = System.getenv("GRADLE_USER_HOME")
			if (path == null) {
				return File(System.getProperty("user.home"), ".gradle")
			}
		}
		return File(path)
	})
}
