import conv.attributes.*
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

/** @see KotlinTarget.disambiguateTarget */
fun KotlinTarget.disambiguateTargetWithItsName() = disambiguateTarget(targetName)

/** @see TARGET_DISAMBIGUATOR_ATTRIBUTE */
fun KotlinTarget.disambiguateTarget(disambiguator: String) {
	attributes.attribute(TARGET_DISAMBIGUATOR_ATTRIBUTE, disambiguator)
}

/** @see KotlinTarget.disambiguateTarget */
fun KotlinSingleTargetExtension<*>.disambiguateTarget(disambiguator: String) {
	target.disambiguateTarget(disambiguator)
}
