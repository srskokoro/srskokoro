pluginManagement {
	includeBuild("gradle/build.logic")
}
plugins {
	id("build.dependencies")
	id("build.dotbuild")
	id("build.settings.buildslist")
}

dependencySettings {
	includeBuild("dependencies")
}

rootProject.name = "srskokoro"
