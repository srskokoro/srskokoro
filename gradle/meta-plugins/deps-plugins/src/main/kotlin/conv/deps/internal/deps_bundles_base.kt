package conv.deps.internal

@Suppress("ClassName")
abstract class deps_bundles_base internal constructor() {

	internal inline fun bundle(init: deps_bundle_spec.() -> Unit) =
		deps_bundle(deps_bundle_spec().apply(init))
}
