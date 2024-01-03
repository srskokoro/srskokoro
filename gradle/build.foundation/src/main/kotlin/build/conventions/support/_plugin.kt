package build.conventions.support

import build.conventions.internal.InternalConventions
import build.conventions.internal.ensureMultipurpose
import build.conventions.internal.ensureReproducibleBuild
import build.conventions.internal.kotlin
import build.conventions.internal.setUpAsSupportForPlugins
import build.conventions.internal.setUpTestTasks
import build.conventions.internal.throwOnNonConventionsRoot
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * NOTE: Replaces the role of [kotlin-dsl-base][org.gradle.kotlin.dsl.plugins.base.KotlinDslBasePlugin].
 */
class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.throwOnNonConventionsRoot()

		InternalConventions.markOrFail(target)

		target.apply {
			plugin(kotlin("jvm"))
			plugin("java-library")
		}

		InternalConventions.ensureReproducibleBuild(target)
		InternalConventions.setUpTestTasks(target)
		InternalConventions.ensureMultipurpose(target)
		InternalConventions.setUpAsSupportForPlugins(target)
	}
}
