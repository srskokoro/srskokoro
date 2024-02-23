package build.foundation.kt.mpp.inclusive

import build.conventions.throwOnNonConventionsRoot
import build.foundation.BuildFoundation
import build.foundation.BuildFoundation.kotlin
import build.foundation.ensureReproducibleBuild
import build.foundation.setUpAsBuildInclusive
import build.foundation.setUpMppHierarchy
import build.foundation.setUpTestTasks
import org.gradle.api.Plugin
import org.gradle.api.Project

class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.throwOnNonConventionsRoot()
		BuildFoundation.markOrFail(target)

		target.apply {
			plugin(kotlin("multiplatform"))
		}

		BuildFoundation.ensureReproducibleBuild(target)
		BuildFoundation.setUpTestTasks(target)
		BuildFoundation.setUpMppHierarchy(target)
		BuildFoundation.setUpAsBuildInclusive(target)
	}
}
