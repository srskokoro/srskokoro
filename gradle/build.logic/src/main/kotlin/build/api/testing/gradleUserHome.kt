package build.api.testing

import java.io.File

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
