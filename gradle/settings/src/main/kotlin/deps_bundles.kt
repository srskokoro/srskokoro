import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("ClassName")
abstract class deps_bundles internal constructor() {
	private val bundles = ConcurrentHashMap<deps_bundle_initializer, deps_bundle>()

	internal fun init(initializer: deps_bundle_initializer) =
		bundles.computeIfAbsent(initializer, initializerComputeIfAbsent)
}

private val initializerComputeIfAbsent =
	java.util.function.Function<deps_bundle_initializer, deps_bundle> { initializer ->
		deps_bundle(deps_bundle_spec().apply(initializer))
	}

private typealias deps_bundle_initializer = deps_bundle_spec.() -> Unit
