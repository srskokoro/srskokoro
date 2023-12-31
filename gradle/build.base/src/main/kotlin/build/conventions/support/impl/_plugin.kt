package build.conventions.support.impl

import build.conventions.internal.InternalConventions
import build.conventions.internal.compileOnlyTestImpl
import build.conventions.internal.ensureMultipurpose
import build.conventions.internal.ensureReproducibleBuild
import build.conventions.internal.kotlin
import build.conventions.internal.setUpTestTasks
import org.gradle.api.HasImplicitReceiver
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.SupportsKotlinAssignmentOverloading
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.assignment.plugin.gradle.AssignmentExtension
import org.jetbrains.kotlin.assignment.plugin.gradle.AssignmentSubplugin
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverGradleSubplugin
import java.io.File

class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.apply_()
	}
}

/**
 * Mimics [kotlin-dsl-base][org.gradle.kotlin.dsl.plugins.base.KotlinDslBasePlugin]
 */
private fun Project.apply_() {
	apply {
		plugin("java-library")
		plugin(kotlin("jvm"))
	}

	mimicKotlinDslCompiler()

	InternalConventions.ensureReproducibleBuild(this)
	InternalConventions.ensureMultipurpose(this)
	InternalConventions.setUpTestTasks(this)

	tasks.withType<Test>().configureEach {
		doFirst {
			// NOTE: Can be used for the Gradle TestKit.
			systemProperty("build.plugins.test.classpath", classpath.joinToString(File.pathSeparator))
		}
	}

	dependencies.run {
		compileOnlyTestImpl(kotlin("reflect"))
		compileOnlyTestImpl(gradleKotlinDsl())
	}
}

/** @see org.gradle.kotlin.dsl.plugins.dsl.KotlinDslCompilerPlugins */
private fun Project.mimicKotlinDslCompiler() {
	with(plugins) {
		apply(SamWithReceiverGradleSubplugin::class.java)
		apply(AssignmentSubplugin::class.java)
	}
	with(extensions) {
		configure<SamWithReceiverExtension> {
			annotation(HasImplicitReceiver::class.qualifiedName!!)
		}
		configure<AssignmentExtension> {
			annotation(SupportsKotlinAssignmentOverloading::class.qualifiedName!!)
		}
	}
}
