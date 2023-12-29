package build.plugins.base.internal

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.api
import build.api.dsl.accessors.compileOnlyTestImpl
import org.gradle.api.HasImplicitReceiver
import org.gradle.api.Project
import org.gradle.api.SupportsKotlinAssignmentOverloading
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.assignment.plugin.gradle.AssignmentExtension
import org.jetbrains.kotlin.assignment.plugin.gradle.AssignmentSubplugin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverGradleSubplugin
import java.io.File

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base.internal._plugin>()
		plugin<build.kt.base.internal._plugin>()
		plugin<build.support.kt.internal._plugin>()
	}

	mimicKotlinDslCompiler()

	tasks.withType<Test>().configureEach {
		doFirst {
			systemProperty("build.plugins.test.classpath", classpath.joinToString(File.pathSeparator))
		}
	}

	configurations.configureEach {
		if (!isCanBeResolved) return@configureEach // Skip

		// Fail on transitive upgrade/downgrade of direct dependency versions
		failOnDirectDependencyVersionGotcha(this, excludeFilter = fun(it): Boolean {
			// Exclude dependencies added automatically by plugins (whether that
			// be our plugins or the built-in ones provided by Gradle).
			run<Unit> {
				if (it.group == "org.jetbrains.kotlin") when (it.name) {
					"kotlin-gradle-plugin" -> return@run
					"kotlin-reflect" -> return@run
					"kotlin-stdlib" -> return@run
					"kotlin-test" -> return@run
					else -> return false // Don't exclude
				}
			}
			return true // Do exclude
		})
	}

	dependencies {
		compileOnlyTestImpl(gradleKotlinDsl())
		/**
		 * NOTE: Our dependency settings plugin should be able to pick up the
		 * following so as to provide the version automatically.
		 *
		 * @see build.dependencies._plugin
		 */
		api(kotlin("gradle-plugin"))
	}
})

/** @see org.gradle.kotlin.dsl.plugins.dsl.KotlinDslCompilerPlugins */
private fun Project.mimicKotlinDslCompiler() {
	plugins.apply(SamWithReceiverGradleSubplugin::class.java)
	x<SamWithReceiverExtension> {
		annotation(HasImplicitReceiver::class.qualifiedName!!)
	}
	plugins.apply(AssignmentSubplugin::class.java)
	x<AssignmentExtension> {
		annotation(SupportsKotlinAssignmentOverloading::class.qualifiedName!!)
	}
}

/**
 * Expected to be used by [build.support.kt.internal._plugin]
 *
 * @see org.gradle.kotlin.dsl.provider.KotlinDslPluginSupport.kotlinCompilerArgs
 */
internal fun KotlinJvmCompilerOptions.mimicKotlinDslCompiler() {
	freeCompilerArgs.apply {
		add("-java-parameters")
		add("-Xjvm-default=all")
		add("-Xjsr305=strict")
		add("-Xsam-conversions=class")
	}
}
