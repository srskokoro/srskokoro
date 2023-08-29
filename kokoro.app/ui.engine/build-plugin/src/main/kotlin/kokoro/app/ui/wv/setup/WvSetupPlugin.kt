package kokoro.app.ui.wv.setup

import conv.internal.setup.*
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.io.File
import java.util.concurrent.Callable

class WvSetupPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.pluginManager.apply("conv.kt.mpp")
		project.pluginManager.apply("conv.kt.mpp.assets")

		// --
		// Set up project extension

		val projectSourceSets = project.kotlinSourceSets
		val wvSetup = project.extensions.create<WvSetupExtension>("wvSetup")

		val wvSetupSourceSets = wvSetup.sourceSets
		// Needed for the generation of type-safe model accessors -- https://docs.gradle.org/8.3/userguide/kotlin_dsl.html#kotdsl:accessor_applicability
		(wvSetup as ExtensionAware).extensions.add(typeOf(), wvSetup::sourceSets.name, wvSetupSourceSets)

		projectSourceSets.configureEach { wvSetupSourceSets.register(name) }

		// --
		// Primary setup

		setUpKtGeneration(project.tasks, projectSourceSets)

		val nonCommonCompilationHook = CompilationHookOverNonCommonTargets(
			project.configurations, projectSourceSets, wvSetupSourceSets,
		)

		project.kotlinMppExt.targets.all {
			if (platformType != KotlinPlatformType.common)
				compilations.all(nonCommonCompilationHook)
		}

		project.afterEvaluate {
			nonCommonCompilationHook.doCopyTargetAttributesAfterEvaluate()
		}
	}
}


private fun setUpKtGeneration(tasks: TaskContainer, projectSourceSets: NamedDomainObjectContainer<KotlinSourceSet>) {
	val generateKtTask = tasks.register<WvSetupGenerateKtTask>("wvSetupGenerateKt") {
		group = LifecycleBasePlugin.BUILD_GROUP

		@Suppress("NAME_SHADOWING") val project = this.project
		outputDir.set(project.layout.buildDirectory.dir(project.provider { "generated/$name" }))

		@Suppress("NAME_SHADOWING") val projectSourceSets = project.kotlinSourceSets
		from(projectSourceSets)
	}

	projectSourceSets.configureEach(fun(s) {
		val name = s.name
		s.kotlin.srcDir(Callable { File(generateKtTask.get().outputDir.get().asFile, name) })
		// ^ NOTE: Task dependency deliberately not wired, in order to avoid
		// dependency cycles.
	})

	tasks.withType<KotlinCompilationTask<*>>().configureEach {
		dependsOn(generateKtTask)
	}
}
