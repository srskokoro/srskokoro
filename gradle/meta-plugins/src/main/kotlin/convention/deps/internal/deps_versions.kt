package convention.deps.internal

@Suppress("ClassName")
internal object deps_versions {
	val plugins = mutableMapOf<String, String>()
	val pluginGroups = mutableMapOf<String, String>()

	val modules = mutableMapOf<Pair<String, String>, String>()
	val moduleGroups = mutableMapOf<String, String>()
}

internal fun deps_versions.plugin(id: String, version: String) {
	plugins[id] = version
}

internal fun deps_versions.pluginGroup(idNs: String, version: String) {
	pluginGroups[idNs] = version
}

internal fun deps_versions.module(moduleNotation: String, version: String) {
	val groupDelimiterIdx = moduleNotation.indexOf(':')
	require(groupDelimiterIdx >= 0) {
		"Supplied `String` module notation \"$moduleNotation\" is invalid. " +
			"Example notations: \"org.gradle:gradle-core\", \"org.mockito:mockito-core\""
	}

	return module(
		group = moduleNotation.substring(0, groupDelimiterIdx),
		name = moduleNotation.substring(groupDelimiterIdx + 1),
		version = version,
	)
}

internal fun deps_versions.module(moduleNotation: Pair<String, String>, version: String) {
	modules[moduleNotation] = version
}

internal fun deps_versions.module(group: String, name: String, version: String) = module(group to name, version)

internal fun deps_versions.moduleGroup(group: String, version: String) {
	moduleGroups[group] = version
}
