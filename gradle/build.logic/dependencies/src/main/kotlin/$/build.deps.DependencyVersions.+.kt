@file:Suppress("PackageDirectoryMismatch")

import build.deps.ModuleId
import build.deps.PluginId
import build.deps.Version

@JvmName("resolvePlugin")
fun Map<PluginId, Version>.resolve(id: String) =
	get(PluginId.of(id)) ?: get(PluginId.ofAnyName(id.substringBeforeLast('.', "")))

@JvmName("resolveModule")
fun Map<ModuleId, Version>.resolve(id: String) =
	get(ModuleId.of(id)) ?: get(ModuleId.ofAnyName(id.substringBefore(':')))
