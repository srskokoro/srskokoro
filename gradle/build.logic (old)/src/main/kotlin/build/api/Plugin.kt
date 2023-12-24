package build.api

/**
 * @see org.gradle.api.Plugin
 */
@JvmDefaultWithoutCompatibility
interface Plugin<T : Any> : org.gradle.api.Plugin<T> {

	@Deprecated("Not meant to be called directly", ReplaceWith("applyTo(target)"), DeprecationLevel.ERROR)
	override fun apply(target: T) = target.applyPlugin()

	/**
	 * @see org.gradle.api.Plugin.apply
	 */
	fun T.applyPlugin()
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> org.gradle.api.Plugin<T>.applyTo(target: T) = apply(target)
