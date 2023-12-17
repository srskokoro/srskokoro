package build.api

/**
 * @see org.gradle.api.Plugin
 */
abstract class PluginApply<T : Any>(
	private val apply: T.() -> Unit,
) : Plugin<T> {

	/**
	 * @see org.gradle.api.Plugin.apply
	 */
	override fun T.applyPlugin() {
		apply.invoke(this)
	}
}
