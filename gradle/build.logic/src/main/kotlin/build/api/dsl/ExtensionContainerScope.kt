package build.api.dsl

import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.*
import kotlin.reflect.KProperty

@JvmInline
value class ExtensionContainerScope(val extensions: ExtensionContainer) {

	inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T {
		return extensions.getByName(property.name) as T
	}

	inline operator fun <reified T> setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		extensions.add(typeOf<T>(), property.name, value!!)
	}

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ExtensionContainerScope = this
}
