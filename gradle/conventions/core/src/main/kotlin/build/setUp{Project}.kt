package build

import build.api.dsl.accessors.kotlin
import build.api.dsl.accessors.kotlinSourceSets
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpTestTasks
import org.gradle.api.Project

internal fun setUp(project: Project): Unit = with(project) {
	setUpAltSrcDirs()
	setUpJvmToolchain(kotlin)

	@OptIn(InternalApi::class)
	BuildFoundation.setUpTestTasks(this)

	kotlinSourceSets.configureEach {
		name.let {
			// TODO! Enable K2 for tests (or upgrade KGP to 2.X) once Kotest finally supports it
			if (it.endsWith("Test") || it == "test") {
				return@configureEach // Early exit. Skip for test source sets.
			}
		}
		// Enable the K2 compiler -- https://kotlinlang.org/docs/whatsnew1920.html#enable-k2-in-gradle
		languageSettings.languageVersion = "2.0"
	}
}
