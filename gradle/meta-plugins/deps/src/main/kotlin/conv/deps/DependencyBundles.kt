package conv.deps

import conv.deps.spec.DependencyBundlesSpec
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.create

abstract class DependencyBundles internal constructor(spec: DependencyBundlesSpec) : ExtensionAware, Iterable<Pair<String, DependencyBundle>> {
	init {
		val extensions: ExtensionContainer = this.extensions
		for ((name, bundle) in spec.bundles)
			extensions.create<DependencyBundle>(name, bundle)
	}

	override fun iterator(): Iterator<Pair<String, DependencyBundle>> = iterator {
		for (it in extensions.extensionsSchema)
			if (it.publicType.concreteClass == DependencyBundle::class.java)
				yield(it.name to extensions.getByName(it.name) as DependencyBundle)
	}
}
