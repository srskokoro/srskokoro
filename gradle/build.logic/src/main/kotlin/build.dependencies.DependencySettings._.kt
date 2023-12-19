import build.dependencies.DependencySettings
import build.dependencies.ModuleId
import build.dependencies.PluginId
import org.gradle.api.InvalidUserDataException

fun DependencySettings.prop(key: String, value: String) {
	if (props.putIfAbsent(key, value) != null)
		throw E_DuplicatePropKey(key)
}

fun DependencySettings.plugin(pluginId: String, version: String) {
	if (plugins.putIfAbsent(PluginId.of(pluginId), version) != null)
		throw E_DuplicatePluginId(pluginId)
}

fun DependencySettings.module(moduleId: String, version: String) {
	if (modules.putIfAbsent(ModuleId.of(moduleId), version) != null)
		throw E_DuplicateModuleId(moduleId)
}

fun DependencySettings.module(group: String, name: String, version: String) {
	val moduleId = ModuleId.of(group, name)
	if (modules.putIfAbsent(moduleId, version) != null)
		throw E_DuplicateModuleId(moduleId)
}

// --

private fun E_DuplicatePropKey(key: Any) = InvalidUserDataException(
	"""
	Cannot add prop "$key" as a prop with that key already exists.
	""".trimIndent()
)

private fun E_DuplicatePluginId(pluginId: Any) = InvalidUserDataException(
	"""
	Cannot add plugin "$pluginId" as a plugin with that name already exists.
	""".trimIndent()
)

private fun E_DuplicateModuleId(moduleId: Any) = InvalidUserDataException(
	"""
	Cannot add module "$moduleId" as a module with that name already exists.
	""".trimIndent()
)
