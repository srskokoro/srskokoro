package conv.deps

import conv.deps.spec.DependencyVersionsSpec
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.create

@Suppress("MemberVisibilityCanBePrivate")
abstract class DependencyVersions internal constructor(spec: DependencyVersionsSpec) : ExtensionAware {
	val jvm: JvmSetup = extensions.create(::jvm.name, spec.jvm)

	val plugins: Map<PluginId, Version> = spec.plugins
	val modules: Map<ModuleId, Version> = spec.modules
}
