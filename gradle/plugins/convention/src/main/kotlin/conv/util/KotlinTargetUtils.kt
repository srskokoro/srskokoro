package conv.util

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

inline val NamedDomainObjectCollection<KotlinTarget>.extensions: ExtensionContainer
	get() = (this as ExtensionAware).extensions
