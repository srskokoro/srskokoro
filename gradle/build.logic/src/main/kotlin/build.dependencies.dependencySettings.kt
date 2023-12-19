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
	get() = x(dependencySettings__name)

fun Settings.dependencySettings(configure: Action<DependencySettings>) = configure.execute(dependencySettings)
inline fun Settings.dependencySettings(configure: DependencySettings.() -> Unit) = configure.invoke(dependencySettings)
