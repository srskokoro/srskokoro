package build.conventions.support

import build.conventions.throwOnNonConventionsRoot
import build.foundation.BuildFoundation
import build.foundation.ensureMultipurpose
import build.foundation.ensureReproducibleBuild
import build.foundation.kotlin
import build.foundation.setUpAsSupportForPlugins
import build.foundation.setUpTestTasks
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * NOTE: Replaces the role of [kotlin-dsl-base][org.gradle.kotlin.dsl.plugins.base.KotlinDslBasePlugin].
 */
class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.throwOnNonConventionsRoot()

		BuildFoundation.markOrFail(target)

		target.apply {
			plugin(kotlin("jvm"))
			plugin("java-library")
		}

		BuildFoundation.ensureReproducibleBuild(target)
		BuildFoundation.setUpTestTasks(target)
		BuildFoundation.ensureMultipurpose(target)
		BuildFoundation.setUpAsSupportForPlugins(target)
	}
}
