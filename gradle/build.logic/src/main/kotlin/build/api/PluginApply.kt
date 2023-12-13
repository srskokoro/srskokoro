package build.api

import org.gradle.api.Plugin

/**
 * @see org.gradle.api.Plugin
 */
abstract class PluginApply<T : Any>(
	private val apply: T.() -> Unit,
) : Plugin<T> {

	/**
	 * @see Plugin.apply
	 */
	override fun apply(target: T) {
		apply.invoke(target)
	}
}
