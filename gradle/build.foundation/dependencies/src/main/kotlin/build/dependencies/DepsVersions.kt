package build.dependencies

import org.gradle.api.UnknownDomainObjectException

class DepsVersions(
	val plugins: Map<PluginId, String>,
	val modules: Map<ModuleId, String>,
) {
	/**
	 * @see DepsVersions.plugins
	 */
	fun plugin(pluginId: String) = plugins[PluginId.of_unsafe(pluginId)] ?: throw E_UnknownPluginId(pluginId)

	/**
	 * @see DepsVersions.modules
	 */
	fun module(moduleId: String) = modules[ModuleId.of_unsafe(moduleId)] ?: throw E_UnknownModuleId(moduleId)

	/**
	 * @see DepsVersions.modules
	 */
	fun module(group: String, name: String) = ModuleId.of_unsafe(group, name).let { modules[it] ?: throw E_UnknownModuleId(it) }
}

internal fun E_UnknownPluginId(pluginId: Any) = UnknownDomainObjectException("Could not find plugin with ID: $pluginId")
internal fun E_UnknownModuleId(moduleId: Any) = UnknownDomainObjectException("Could not find module with ID: $moduleId")
