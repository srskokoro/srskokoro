import conv.attributes.*
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

/** @see KotlinTarget.disambiguateTarget */
fun KotlinTarget.disambiguateTargetWithItsName() = disambiguateTarget(targetName)

/**
 * Used for distinguishing several targets for one platform.
 *
 * See, “[Distinguish several targets for one platform | Set up targets for Kotlin Multiplatform | Kotlin Documentation](https://kotlinlang.org/docs/multiplatform-set-up-targets.html#distinguish-several-targets-for-one-platform)”
 */
fun KotlinTarget.disambiguateTarget(disambiguator: String) {
	attributes.attribute(TARGET_DISAMBIGUATOR_ATTRIBUTE, disambiguator)
}

/** @see KotlinTarget.disambiguateTarget */
fun KotlinSingleTargetExtension<*>.disambiguateTarget(disambiguator: String) {
	target.disambiguateTarget(disambiguator)
}
