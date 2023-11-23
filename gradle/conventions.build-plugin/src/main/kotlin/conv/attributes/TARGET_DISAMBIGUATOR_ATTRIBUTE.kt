package conv.attributes

import org.gradle.api.attributes.Attribute

/**
 * Used for distinguishing several targets for one platform.
 *
 * See, “[Distinguish several targets for one platform | Set up targets for Kotlin Multiplatform | Kotlin Documentation](https://kotlinlang.org/docs/multiplatform-set-up-targets.html#distinguish-several-targets-for-one-platform)”
 */
val TARGET_DISAMBIGUATOR_ATTRIBUTE: Attribute<String> =
	Attribute.of("conv.kt.mpp.targetDisambiguator", String::class.java)
