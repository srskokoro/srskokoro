import build.dependencies.DependencySettings
import build.dependencies.KotlinId
import build.dependencies.ModuleId
import build.dependencies.PluginId
import org.gradle.api.InvalidUserDataException

fun DependencySettings.prop(key: String, value: Any) = prop(key, value.toString())

fun DependencySettings.prop(key: String, value: String) {
	if (props.putIfAbsent(key, value) != null)
		throw E_DuplicatePropKey(key)
}

/**
 * @see KotlinId
 * @see KotlinId.pluginId
 * @see KotlinId.moduleId
 */
@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
inline fun DependencySettings.kotlin(module: String) = KotlinId(module)

fun DependencySettings.plugin(pluginId: KotlinId, version: String) =
	plugin(pluginId.pluginId(), version)

fun DependencySettings.plugin(pluginId: String, version: String) {
	if (plugins.putIfAbsent(PluginId.of(pluginId), version) != null)
		throw E_DuplicatePluginId(pluginId)
}

fun DependencySettings.module(moduleId: KotlinId, version: String) =
	module(moduleId.moduleId(), version)

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

private fun E_DuplicateEntry(kind: String, name: Any, nameKind: String) = InvalidUserDataException(
	"Cannot add $kind \"$name\" as a $kind with that $nameKind already exists."
)

private fun E_DuplicatePropKey(key: String) = E_DuplicateEntry("prop", key, "key")

private fun E_DuplicatePluginId(pluginId: Any) = E_DuplicateEntry("plugin", pluginId, "name")

private fun E_DuplicateModuleId(moduleId: Any) = E_DuplicateEntry("module", moduleId, "name")
