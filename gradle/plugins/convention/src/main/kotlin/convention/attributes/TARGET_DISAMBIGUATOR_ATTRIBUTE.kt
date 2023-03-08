package convention.attributes

import org.gradle.api.attributes.Attribute

val TARGET_DISAMBIGUATOR_ATTRIBUTE: Attribute<String> =
	Attribute.of("convention.kotlin.mpp.targetDisambiguator", String::class.java)
