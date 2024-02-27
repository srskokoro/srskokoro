@file:Suppress("NOTHING_TO_INLINE")

package build.dependencies

/**
 * @see pluginId
 * @see moduleId
 */
@JvmInline
value class KotlinId(val module: String) {

	inline fun pluginId() = "org.jetbrains.kotlin.$module"

	inline fun moduleId() = "org.jetbrains.kotlin:kotlin-$module"
}
