package build.foundation.kt.mpp.multipurpose

import build.conventions.throwOnNonConventionsRoot
import build.foundation.BuildFoundation
import build.foundation.ensureMultipurpose
import build.foundation.ensureReproducibleBuild
import build.foundation.kotlin
import build.foundation.setUpMppLibTargets
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
		BuildFoundation.setUpMppLibTargets(target)
		BuildFoundation.ensureMultipurpose(target)
	}
}
