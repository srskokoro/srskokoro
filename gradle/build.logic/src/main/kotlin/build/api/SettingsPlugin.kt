package build.api

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * @see org.gradle.api.Plugin
 */
interface SettingsPlugin : Plugin<Settings> {

	override fun apply(settings: Settings)
}
