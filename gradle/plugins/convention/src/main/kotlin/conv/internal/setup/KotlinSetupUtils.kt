package conv.internal.setup

import conv.internal.support.unsafeCast
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal fun getSourceSets(kotlin: KotlinProjectExtension): NamedDomainObjectContainer<KotlinSourceSet> {
	// It's more efficient to get it this way, since `kotlin.sourceSets`
	// allocates a new object every time (as of Kotlin Gradle Plugin 1.8.0)
	// - Also throws if our assumption (that it's an extension) is incorrect.
	return getKotlinSourceSets((kotlin as ExtensionAware).extensions)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun getKotlinSourceSets(kotlinExtensions: ExtensionContainer): NamedDomainObjectContainer<KotlinSourceSet> {
	return kotlinExtensions.getByName("sourceSets").unsafeCast()
}
