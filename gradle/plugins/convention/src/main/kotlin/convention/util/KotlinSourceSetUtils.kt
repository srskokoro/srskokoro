package convention.util

import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

fun NamedDomainObjectProvider<out KotlinSourceSet>.dependencies(
	configure: KotlinDependencyHandler.() -> Unit
) {
	configure {
		dependencies(configure)
	}
}

fun DomainObjectCollection<out KotlinSourceSet>.dependencies(
	configure: KotlinDependencyHandler.() -> Unit
) {
	all {
		dependencies(configure)
	}
}
