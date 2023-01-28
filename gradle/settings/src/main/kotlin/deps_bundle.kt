@Suppress("ClassName")
class deps_bundle internal constructor(from: deps_bundle_spec) {
	val modules: Set<String> = from.modulesSeq.toSet()

	inline fun modules(action: (String) -> Unit) = modules.forEach(action)

	inline operator fun invoke(action: (String) -> Unit) = modules(action)
}
