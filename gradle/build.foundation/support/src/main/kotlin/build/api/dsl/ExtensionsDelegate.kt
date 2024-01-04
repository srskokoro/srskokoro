package build.api.dsl

import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.*
import kotlin.reflect.KProperty

/**
 * @param O the type of the owner that owns the [extensions]; useful for
 * creating extension functions that targets [ExtensionsDelegate].
 *
 * @see x
 */
@JvmInline
value class ExtensionsDelegate<O>(val extensions: ExtensionContainer) {

	inline operator fun <reified R> getValue(thisRef: Any?, property: KProperty<*>): R {
		return extensions.getByName(property.name) as R
	}

	inline operator fun <reified R> setValue(thisRef: Any?, property: KProperty<*>, value: R) {
		extensions.add(typeOf<R>(), property.name, value!!)
	}

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ExtensionsDelegate<O> = this
}
