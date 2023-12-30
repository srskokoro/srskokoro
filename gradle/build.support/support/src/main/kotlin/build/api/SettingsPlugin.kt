package build.api

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * @see org.gradle.api.Plugin
 */
abstract class SettingsPlugin(
	private val apply: Settings.() -> Unit = {},
) : Plugin<Settings> {
	override fun apply(target: Settings) {
		apply.invoke(target)
	}
}
