import build.api.dsl.*
import build.dependencies.DependencySettings
import org.gradle.api.Action
import org.gradle.api.initialization.Settings

/**
 * NOTE: Can't use `::`[dependencySettings]`.name` due to overload ambiguity. So
 * we created this constant.
 */
internal const val dependencySettings__name = "dependencySettings"

val Settings.dependencySettings: DependencySettings
	get() = xs().getSafelyOrNull(dependencySettings__name) ?: error(
		"Settings plugin \"" +
			build.dependencies._plugin::class.java.packageName +
			"\" not yet applied."
	)

// NOTE: Correct line numbers are reported only when `Action<T>` is used --
// i.e., we can't even use `() -> Unit` here (`inline` or not).
// - This restriction seem to apply only to calls made directly at the top-level
// of the kotlin script (`*.kts`) file.
fun Settings.dependencySettings(configure: Action<DependencySettings>) = configure.execute(dependencySettings)
