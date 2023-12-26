package build.plugins.test

import java.io.File

val gradleUserHome = run(fun(): File {
	/**
	 * @see org.gradle.testkit.runner.GradleRunner.withGradleVersion
	 */
	var path = System.getProperty("gradle.user.home")
	if (path == null) {
		path = System.getenv("GRADLE_USER_HOME")
		if (path == null) {
			return File(System.getProperty("user.home"), ".gradle")
		}
	}
	return File(path)
})
