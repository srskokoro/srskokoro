package build.api.dsl

import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.initialization.IncludedBuild
import org.gradle.api.invocation.Gradle

@Suppress("NOTHING_TO_INLINE")
inline fun Gradle.includedBuilds(vararg names: String) = includedBuilds(names.asList())

fun Gradle.includedBuilds(names: Collection<String>) = mutableListOf<IncludedBuild>().also { out ->
	@Suppress("NAME_SHADOWING") val names = LinkedHashSet(names)
	for (build in includedBuilds) {
		if (names.remove(build.name)) {
			out.add(build)
		}
	}
	if (names.isNotEmpty()) {
		throw UnknownDomainObjectException("Included build '${names.first()}' not found in $this")
	}
}
