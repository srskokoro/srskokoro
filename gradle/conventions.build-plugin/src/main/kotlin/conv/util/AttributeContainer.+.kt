package conv.util

import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer

fun <T : Any> AttributeContainer.attributeFrom(from: AttributeContainer, key: Attribute<T>): AttributeContainer {
	attribute(key, checkNotNull(from.getAttribute(key)))
	return this
}

fun AttributeContainer.attributesFrom(from: AttributeContainer, keys: Iterable<Attribute<*>>): AttributeContainer {
	for (key in keys) attributeFrom(from, key)
	return this
}

fun AttributeContainer.attributesFrom(from: AttributeContainer) = attributesFrom(from, from.keySet())
