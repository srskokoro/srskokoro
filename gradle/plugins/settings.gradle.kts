pluginManagement {
	includeBuild("../build.logic")
	includeBuild("../conventions.base")
}
plugins {
	id("build.dotbuild")
}

dependencyResolutionManagement {
	includeBuild("../conventions.plugins")
}
