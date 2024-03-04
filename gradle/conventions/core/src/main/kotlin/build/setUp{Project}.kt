package build

import build.api.dsl.accessors.kotlin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpTestTasks
import org.gradle.api.Project

internal fun setUp(project: Project): Unit = with(project) {
	setUpAltSrcDirs()
	setUpJvmToolchain(kotlin)

	@OptIn(InternalApi::class)
	BuildFoundation.setUpTestTasks(this)
}
