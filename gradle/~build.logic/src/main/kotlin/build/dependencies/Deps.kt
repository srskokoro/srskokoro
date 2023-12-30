package build.dependencies

@Suppress("MemberVisibilityCanBePrivate")
class Deps(
	val props: DepsProps,
	val versions: DepsVersions,
) {
	fun prop(key: String) = props[key]
}
