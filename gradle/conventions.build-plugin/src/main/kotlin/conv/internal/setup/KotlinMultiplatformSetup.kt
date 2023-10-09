package conv.internal.setup

import conv.internal.KotlinTargetsConfigLoader
import conv.internal.skipPlaceholderGenerationForKotlinTargetsConfigLoader
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.typeOf
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal fun Project.setUp(kotlin: KotlinMultiplatformExtension) {
	setUp(kotlin as KotlinProjectExtension)

	// The following makes sure that the type-safe accessors for extensions
	// added to `targets` are generated. See also, "Understanding when type-safe
	// model accessors are available | Gradle Kotlin DSL Primer | 7.5.1" --
	// https://docs.gradle.org/7.5.1/userguide/kotlin_dsl.html#kotdsl:accessor_applicability
	//
	// NOTE: If one day, this would cause an exception due to "targets" already
	// existing, simply remove the following. It's likely that it's already
	// implemented for us, and if so, we shouldn't need to do anything.
	(kotlin as ExtensionAware).extensions.add(typeOf(), "targets", kotlin.targets)
}

internal fun Project.setUpTargetsExtensions(kotlin: KotlinMultiplatformExtension) {
	val config = layout.projectDirectory.file("build.targets.cf")
	KotlinTargetsConfigLoader(providers, config).loadInto(kotlin, skipPlaceholderGenerationForKotlinTargetsConfigLoader)
}
