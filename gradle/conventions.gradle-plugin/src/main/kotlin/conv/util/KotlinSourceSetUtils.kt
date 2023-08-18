package conv.util

import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

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
	if (parentMain.name != KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME) {
		val commonMain = getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
		newMain.dependsOn(commonMain)
	}
	if (parentTest.name != KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME) {
		val commonTest = getByName(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME)
		newTest.dependsOn(commonTest)
	}

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

//region Utilities for inspecting `KotlinSourceSet` trees

fun Iterable<KotlinSourceSet>.toDependencyMap(): MutableMap<String, MutableMap<String, KotlinSourceSet>> {
	val out = LinkedHashMap<String, MutableMap<String, KotlinSourceSet>>()
	for (entry in this) {
		val entryName = entry.name
		for (parent in entry.dependsOn) {
			out.computeIfAbsent(parent.name) { LinkedHashMap() }
				.putIfAbsent(entryName, entry)
		}
	}
	return out
}

fun KotlinSourceSetContainer.printSourceSetTrees(out: Appendable = System.out) {
	sourceSets.printSourceSetTrees(out)
}

fun Iterable<KotlinSourceSet>.printSourceSetTrees(out: Appendable = System.out) {
	val printed = HashSet<String>()
	val dependencyMap = toDependencyMap()

	val entryNames = ArrayDeque<String>(if (this is Collection<*>) size else 0)
	for (entry in this) when (val entryName = entry.name) {
		KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME,
		KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME,
		-> entryNames.addFirst(entryName)
		else -> entryNames.addLast(entryName)
	}

	for (entryName in entryNames) printSourceSetTrees_impl(
		entryName,
		out = out,
		prefix = "",
		printed = printed,
		dependencyMap,
	)
}

private fun printSourceSetTrees_impl(
	entryName: String,
	out: Appendable,
	prefix: String,
	printed: HashSet<String>,
	dependencyMap: MutableMap<String, out MutableMap<String, KotlinSourceSet>>,
) {
	out.append(prefix)
	out.append("' ")
	out.append(entryName)
	if (!printed.add(entryName)) {
		out.appendLine(" (*)")
	} else {
		out.appendLine()
		dependencyMap[entryName]?.forEach { (childName, _) ->
			printSourceSetTrees_impl(
				childName,
				out = out,
				prefix = "$prefix| ",
				printed = printed,
				dependencyMap
			)
		}
	}
}

//endregion
