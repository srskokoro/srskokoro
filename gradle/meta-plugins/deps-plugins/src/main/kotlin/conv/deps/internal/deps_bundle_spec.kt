package conv.deps.internal

import conv.deps.internal.util.indexOfModuleGroupDelimiter

@Suppress("ClassName", "MemberVisibilityCanBePrivate")
internal class deps_bundle_spec internal constructor() {
	val modules = mutableMapOf<Pair<String, String>, String>()
	val modulesSeq = modules.asSequence().map { (moduleNotation, version) ->
		val (group, name) = moduleNotation
		if (version.isEmpty()) "$group:$name"
		else "$group:$name:$version"
	}
}

internal fun deps_bundle_spec.module(dependencyNotation: String) {
	val groupDelimiterIdx = indexOfModuleGroupDelimiter(isDependencyNotation = true, dependencyNotation)
	val nameDelimiterIdx = dependencyNotation.indexOf(':', groupDelimiterIdx + 1)

	val group = dependencyNotation.substring(0, groupDelimiterIdx)
	val name: String
	val version: String

	if (nameDelimiterIdx < 0) {
		name = dependencyNotation.substring(groupDelimiterIdx + 1)
		version = ""
	} else {
		name = dependencyNotation.substring(groupDelimiterIdx + 1, nameDelimiterIdx)
		version = dependencyNotation.substring(nameDelimiterIdx + 1)
	}

	return module(
		group = group,
		name = name,
		version = version,
	)
}

internal fun deps_bundle_spec.module(moduleNotation: String, version: String) {
	val groupDelimiterIdx = indexOfModuleGroupDelimiter(isDependencyNotation = false, moduleNotation)
	return module(
		group = moduleNotation.substring(0, groupDelimiterIdx),
		name = moduleNotation.substring(groupDelimiterIdx + 1),
		version = version,
	)
}

internal fun deps_bundle_spec.module(moduleNotation: Pair<String, String>, version: String) {
	modules[moduleNotation] = version
}

internal fun deps_bundle_spec.module(group: String, name: String, version: String) = module(group to name, version)
