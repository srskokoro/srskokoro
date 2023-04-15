import conv.version.InternalVersionLoader

plugins {
	id("org.ajoberstar.grgit.service")
}

run<Unit> {
	// Named like this to discourage direct access
	val versionLoader__name = "--internal-version-loader"
	val versionLoader = rootProject.extensions.let { xs ->
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		xs.findByName(versionLoader__name) as InternalVersionLoader?
		?: xs.create(versionLoader__name, grgitService.service)
	}
	versionLoader.version?.let { version = it }
	versionCode = versionLoader.versionCode
}
