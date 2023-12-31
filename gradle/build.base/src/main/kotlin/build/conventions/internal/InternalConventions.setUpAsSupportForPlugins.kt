package build.conventions.internal

import org.gradle.api.HasImplicitReceiver
import org.gradle.api.Project
import org.gradle.api.SupportsKotlinAssignmentOverloading
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.assignment.plugin.gradle.AssignmentExtension
import org.jetbrains.kotlin.assignment.plugin.gradle.AssignmentSubplugin
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverGradleSubplugin
import java.io.File

/**
 * WARNING: Assumes that [ensureMultipurpose][build.conventions.internal.ensureMultipurpose]`()`
 * has already been called beforehand.
 */
fun InternalConventions.setUpAsSupportForPlugins(project: Project): Unit = with(project) {
	mimicKotlinDslCompiler()

	tasks.withType<Test>().configureEach {
		doFirst {
			// NOTE: Can be used for the Gradle TestKit.
			systemProperty("build.plugins.test.classpath", classpath.joinToString(File.pathSeparator))
		}
	}

	dependencies.run {
		compileOnlyTestImpl(embeddedKotlin("reflect"))
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
		configure<SamWithReceiverExtension>("samWithReceiver") {
			annotation(HasImplicitReceiver::class.qualifiedName!!)
		}
		configure<AssignmentExtension>("assignment") {
			annotation(SupportsKotlinAssignmentOverloading::class.qualifiedName!!)
		}
	}
}
