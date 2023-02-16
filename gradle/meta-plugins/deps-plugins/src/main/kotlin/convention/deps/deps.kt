@file:Suppress("PackageDirectoryMismatch")

import convention.deps.internal.deps_jvm
import convention.deps.internal.deps_versions

@Suppress("ClassName")
object deps {
	object bundles : deps_bundles()
	object jvm : deps_jvm()

	val plugins: Map<String, String> get() = deps_versions.plugins
	val pluginGroups: Map<String, String> get() = deps_versions.pluginGroups

	val modules: Map<Pair<String, String>, String> get() = deps_versions.modules
	val moduleGroups: Map<String, String> get() = deps_versions.moduleGroups

	init {
		deps_versions.init()
	}
}
