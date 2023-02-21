package convention.internal.util

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal fun getSourceSets(kotlin: KotlinProjectExtension): NamedDomainObjectContainer<KotlinSourceSet> {
	// It's more efficient to get it this way. Also throws if our assumption (that it's an extension) is incorrect.
	return getKotlinSourceSets((kotlin as ExtensionAware).extensions)
}

internal fun getKotlinSourceSets(kotlinExtension: ExtensionContainer): NamedDomainObjectContainer<KotlinSourceSet> {
	@Suppress("UNCHECKED_CAST") return kotlinExtension.getByName("sourceSets") as NamedDomainObjectContainer<KotlinSourceSet>
}
