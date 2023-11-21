@file:Suppress("PackageDirectoryMismatch")

import conv.deps.ModuleId
import conv.deps.PluginId
import conv.deps.Version

@JvmName("resolvePlugin")
fun Map<PluginId, Version>.resolve(id: String) =
	get(PluginId.of(id)) ?: get(PluginId.ofAnyName(id.substringBeforeLast('.', "")))

@JvmName("resolveModule")
fun Map<ModuleId, Version>.resolve(id: String) =
	get(ModuleId.of(id)) ?: get(ModuleId.ofAnyName(id.substringBefore(':')))
