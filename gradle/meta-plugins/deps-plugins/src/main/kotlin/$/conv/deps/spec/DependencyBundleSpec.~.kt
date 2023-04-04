@file:Suppress("PackageDirectoryMismatch")

import conv.deps.ModuleId
import conv.deps.Version
import conv.deps.failOnDuplicateModuleId
import conv.deps.spec.DependencyBundleSpec

fun DependencyBundleSpec.module(moduleId: String) = moduleImpl(moduleId)
fun DependencyBundleSpec.module(moduleId: Any) = moduleImpl(moduleId)

fun DependencyBundleSpec.module(moduleId: String, version: String) = moduleImpl(moduleId, version)
fun DependencyBundleSpec.module(moduleId: Any, version: Any) = moduleImpl(moduleId, version)

@Suppress("NOTHING_TO_INLINE") private inline fun DependencyBundleSpec.moduleImpl(
	moduleId: String,
	version: String? = null,
) = moduleImpl({ ModuleId.of(moduleId) }, { version?.let { Version.of(it) } })

@Suppress("NOTHING_TO_INLINE") private inline fun DependencyBundleSpec.moduleImpl(
	moduleId: Any,
	version: Any? = null,
) = moduleImpl({ ModuleId.of(moduleId) }, { version?.let { Version.of(it) } })

private inline fun DependencyBundleSpec.moduleImpl(moduleIdFn: () -> ModuleId, versionFn: () -> Version?) {
	val map = modules
	val size = map.size

	val moduleId = moduleIdFn()
	val version = versionFn()

	map.putIfAbsent(moduleId, version)

	if (size == map.size) {
		failOnDuplicateModuleId(moduleId)
	}
}
