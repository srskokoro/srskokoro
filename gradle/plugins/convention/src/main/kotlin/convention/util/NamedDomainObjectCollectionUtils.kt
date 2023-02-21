package convention.util

import org.gradle.api.DomainObjectCollection
import org.gradle.kotlin.dsl.withType
import kotlin.reflect.KClass

/**
 * @see DomainObjectCollection.withType
 */
inline fun <T : Any, D : DomainObjectCollection<in T>> D.onType(
	type: KClass<T>, crossinline configuration: T.() -> Unit
): D {
	withType(type.java) { configuration() }
	return this
}
