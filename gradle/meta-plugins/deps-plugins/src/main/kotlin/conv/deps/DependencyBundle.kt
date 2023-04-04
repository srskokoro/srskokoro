package conv.deps

import conv.deps.spec.DependencyBundleSpec
import org.gradle.api.plugins.ExtensionAware

abstract class DependencyBundle internal constructor(spec: DependencyBundleSpec) : ExtensionAware, Iterable<String> {
	val modules: Set<String> = spec.modules
		.mapTo(HashSet()) { (moduleId, version) -> moduleId.toString(version) }

	override fun iterator() = modules.iterator()

	inline operator fun times(crossinline action: (String) -> Unit) = timesAssign(action)

	inline operator fun timesAssign(crossinline action: (String) -> Unit) = modules.forEach(action)
}
