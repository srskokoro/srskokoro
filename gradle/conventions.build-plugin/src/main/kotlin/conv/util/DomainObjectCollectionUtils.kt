package conv.util

import org.gradle.api.DomainObjectCollection
import org.gradle.kotlin.dsl.withType
import kotlin.reflect.KClass

/**
 * @see DomainObjectCollection.withType
 */
inline fun <reified T : Any, D : DomainObjectCollection<in T>> D.onType(
	@Suppress("UNUSED_PARAMETER") type: KClass<T>,
	crossinline configuration: T.() -> Unit,
): D {
	all { if (this is T) configuration() }
	return this
}

/**
 * @see DomainObjectCollection.withType
 */
inline fun <reified T : Any, D : DomainObjectCollection<in T>> D.onType(
	@Suppress("UNUSED_PARAMETER") type: KClass<T>,
	crossinline filter: (T) -> Boolean,
	crossinline configuration: T.() -> Unit,
): D {
	all {
		if (this is T && filter(this)) {
			configuration()
		}
	}
	return this
}
