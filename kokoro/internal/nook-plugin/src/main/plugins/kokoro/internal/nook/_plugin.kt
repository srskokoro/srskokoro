package kokoro.internal.nook

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlinSourceSets
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

private const val REQUIRED_OPT_IN_CLASS = "kokoro.internal.NookContract"

class _plugin : ProjectPlugin({
	tasks.withType<KotlinCompilationTask<*>>().configureEach {
		compilerOptions.freeCompilerArgs.run {
			// NOTE: Android Studio honors `languageSettings.optIn()` (from
			// `KotlinSourceSet`) but only for KMP :P
			add("-opt-in=$REQUIRED_OPT_IN_CLASS")
		}
	}
	// NOTE: We're using `prioritizedAfterEvaluate()` here since KGP may not
	// have been applied yet.
	prioritizedAfterEvaluate {
		kotlinSourceSets.configureEach {
			// NOTE: Android Studio doesn't honor `-opt-in` compiler option when
			// in KMP :P
			languageSettings.optIn(REQUIRED_OPT_IN_CLASS)
		}
	}
})
