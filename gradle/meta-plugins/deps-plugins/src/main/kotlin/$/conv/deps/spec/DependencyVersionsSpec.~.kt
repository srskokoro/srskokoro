@file:Suppress("PackageDirectoryMismatch")

import conv.deps.*
import conv.deps.spec.DependencyVersionsSpec
import conv.deps.spec.JvmSetupSpec


inline fun DependencyVersionsSpec.jvm(crossinline config: JvmSetupSpec.() -> Unit) {
	jvm.config()
}


fun DependencyVersionsSpec.plugin(pluginId: String, version: String) {
	if (plugins.putIfAbsent(PluginId.of(pluginId), Version.of(version)) != null)
		failOnDuplicatePluginId(pluginId)
}

fun DependencyVersionsSpec.plugin(pluginId: Any, version: Any) {
	val id = PluginId.of(pluginId)
	if (plugins.putIfAbsent(id, Version.of(version)) != null)
		failOnDuplicatePluginId(id)
}


fun DependencyVersionsSpec.module(moduleId: String, version: String) {
	if (modules.putIfAbsent(ModuleId.of(moduleId), Version.of(version)) != null)
		failOnDuplicateModuleId(moduleId)
}

fun DependencyVersionsSpec.module(moduleId: Any, version: Any) {
	val id = ModuleId.of(moduleId)
	if (modules.putIfAbsent(id, Version.of(version)) != null)
		failOnDuplicateModuleId(id)
}
