@Suppress("ClassName")
class deps_bundle internal constructor(spec: deps_bundle_spec) {
	val modules: Set<String> = spec.modulesSeq.toSet()

	inline fun modules(action: (String) -> Unit) = modules.forEach(action)

	inline operator fun invoke(action: (String) -> Unit) = modules(action)
}
