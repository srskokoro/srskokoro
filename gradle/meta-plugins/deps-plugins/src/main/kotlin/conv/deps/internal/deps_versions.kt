package conv.deps.internal

import conv.deps.internal.util.indexOfModuleGroupDelimiter

@Suppress("ClassName")
internal object deps_versions {
	val jvm = deps_jvm_spec()

	val plugins = mutableMapOf<String, String>()
	val pluginGroups = mutableMapOf<String, String>()

	val modules = mutableMapOf<Pair<String, String>, String>()
	val moduleGroups = mutableMapOf<String, String>()
}

internal inline fun deps_versions.jvm(config: deps_jvm_spec.() -> Unit) {
	jvm.config()
}

internal fun deps_versions.plugin(id: String, version: String) {
	plugins[id] = version
}

internal fun deps_versions.pluginGroup(idNs: String, version: String) {
	pluginGroups[idNs] = version
}

internal fun deps_versions.module(moduleNotation: String, version: String) {
	val groupDelimiterIdx = indexOfModuleGroupDelimiter(isDependencyNotation = false, moduleNotation)
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
