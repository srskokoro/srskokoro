package conv.util

import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

infix fun List<KotlinSourceSet>.dependsOn(others: List<KotlinSourceSet>) {
	forEachIndexed { i, it -> it.dependsOn(others[i]) }
}

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
