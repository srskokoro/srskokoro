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
	newTest.dependsOn(parentTest)

	// The IDE complains about expect-actual declarations in our tests. For some
	// unknown reason, the following voodoo magic fixes that. NOTE: We also
	// tried to wire the newly created test source set to the newly created main
	// source set, but that made things worse: it introduced some hard-to-
	// describe build errors.
	for (it in parentMain.dependsOn) newMain.dependsOn(it)
	for (it in parentTest.dependsOn) newTest.dependsOn(it)

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
