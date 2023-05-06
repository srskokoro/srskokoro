package conv.util

import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

infix fun List<KotlinSourceSet>.dependsOn(others: List<KotlinSourceSet>) {
	forEachIndexed { i, it -> it.dependsOn(others[i]) }
}

fun NamedDomainObjectContainer<out KotlinSourceSet>.derive(
	namePrefix: String,
	parentMain: KotlinSourceSet,
	parentTest: KotlinSourceSet,
) = kotlin.run {
	val newMain = create(namePrefix + "Main")
	newMain.dependsOn(parentMain)

	val newTest = create(namePrefix + "Test")
	newTest.dependsOn(newMain)
	newTest.dependsOn(parentTest)

	newMain to newTest
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
