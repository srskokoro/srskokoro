import conv.version.InternalVersion
import conv.version.InternalVersionLoader

run<Unit> {
	val rootProject = rootProject
	val xs = rootProject.extensions

	// Named like this to discourage direct access
	val internalVersion__name = "--internal-version"
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	var internalVersion = xs.findByName(internalVersion__name) as InternalVersion?
	if (internalVersion == null) {
		val providers = rootProject.providers

		@Suppress("UnstableApiUsage", "USELESS_CAST")
		internalVersion = providers.of(InternalVersionLoader::class) {
			val parameters = parameters
			parameters.rootProjectDir.set(rootProject.layout.projectDirectory)
			parameters.releasing.set(providers.isReleasing)
		}.get() as InternalVersion

		xs.add(internalVersion__name, internalVersion)
	}

	internalVersion.name?.let { versionName = it }
	versionCode = internalVersion.code
}
