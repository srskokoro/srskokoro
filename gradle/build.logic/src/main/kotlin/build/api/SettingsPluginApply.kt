package build.api

import org.gradle.api.initialization.Settings

/**
 * @see org.gradle.api.Plugin
 */
abstract class SettingsPluginApply(
	apply: Settings.() -> Unit,
) : SettingsPlugin, PluginApply<Settings>(apply) {

	@Suppress("RedundantOverride", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
	override fun apply(settings: Settings) = super.apply(settings)
}
