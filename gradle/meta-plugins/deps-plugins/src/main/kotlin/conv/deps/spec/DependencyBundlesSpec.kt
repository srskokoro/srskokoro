package conv.deps.spec

import conv.deps.failOnBundleName
import conv.deps.serialization.cannotStore
import org.gradle.api.plugins.ExtensionAware

abstract class DependencyBundlesSpec internal constructor() : ExtensionAware {
	val bundles: MutableMap<String, DependencyBundleSpec> = HashMap()

	fun bundle(name: String): DependencyBundleSpec {
		if (cannotStore(name)) {
			failOnBundleName(name)
		}
		return bundle_unsafe(name)
	}

	@Suppress("NOTHING_TO_INLINE")
	internal inline fun bundle_unsafe(name: String) =
		bundles.computeIfAbsent(name) { DependencyBundleSpec() }

	inline fun bundle(name: String, crossinline config: DependencyBundleSpec.() -> Unit) = bundle(name).config()
}
