package kokoro.internal.scoping

import build.api.dsl.*
import build.api.dsl.accessors.kotlin
import build.api.dsl.accessors.kotlinSourceSets
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

private const val REQUIRED_OPT_IN_CLASS = "kokoro.internal.RequiresCompilerPlugin"

class _plugin : KotlinCompilerPluginSupportPlugin {
	override fun apply(target: Project) {
		target.apply_()
	}

	override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

	override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
		return kotlinCompilation.target.project.provider {
			listOf() // No options.
		}
	}

	override fun getCompilerPluginId() = COMPILER_PLUGIN_ID

	override fun getPluginArtifact() = SubpluginArtifact(
		COMPILER_ARTIFACT_GROUP,
		COMPILER_ARTIFACT_NAME,
	)
}

private fun Project.apply_() {
	tasks.withType<KotlinCompilationTask<*>>().configureEach {
		compilerOptions.freeCompilerArgs.run {
			// NOTE: Android Studio honors `languageSettings.optIn()` (from
			// `KotlinSourceSet`) but only for KMP :P
			add("-opt-in=$REQUIRED_OPT_IN_CLASS")
		}
	}
	prioritizedAfterEvaluate {
		val kotlin = kotlin
		kotlin.kotlinSourceSets.all {
			// NOTE: Android Studio doesn't honor `-opt-in` compiler option when
			// in KMP :P
			languageSettings.optIn(REQUIRED_OPT_IN_CLASS)
		}
		when (kotlin) {
			is KotlinSingleTargetExtension<*> -> "implementation"
			is KotlinMultiplatformExtension -> "commonMainImplementation"
			else -> error("Unexpected `kotlin` extension $kotlin")
		}.let {
			dependencies.add(it, RUNTIME_ARTIFACT_COORDINATE)
		}
	}
}
