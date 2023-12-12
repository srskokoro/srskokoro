package build.support.gradle

/**
 * @see org.gradle.api.Plugin
 */
abstract class Plugin<T : Any>(
	private val apply: T.() -> Unit,
) : org.gradle.api.Plugin<T> {

	/**
	 * @see org.gradle.api.Plugin.apply
	 */
	override fun apply(target: T) {
		apply.invoke(target)
	}
}
