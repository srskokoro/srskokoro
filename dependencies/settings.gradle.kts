pluginManagement {
	includeBuild("../gradle/build.logic")
}
plugins {
	id("build.dependencies")
}

dependencySettings {
	exportOnly()
}
