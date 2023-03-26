package conv.attributes

import org.gradle.api.attributes.Attribute

val TARGET_DISAMBIGUATOR_ATTRIBUTE: Attribute<String> =
	Attribute.of("convention:conv.kt.mpp.targetDisambiguator", String::class.java)
