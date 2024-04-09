package build

import build.api.dsl.accessors.kotlin
import build.foundation.BuildFoundation
import build.foundation.InternalApi
import build.foundation.setUpTestTasks
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile as KotlinJvmCompileTask

internal fun setUp(project: Project): Unit = with(project) {
	setUpAltSrcDirs()
	setUpJvmToolchain(kotlin)

	tasks.withType<KotlinJvmCompileTask>().configureEach {
		setUp(compilerOptions)
	}

	@OptIn(InternalApi::class)
	BuildFoundation.setUpTestTasks(this)
}
