pluginManagement {
	includeBuild("gradle/build.base")
}
plugins {
	//id("build.dependencies")
	id("build.dotbuild")
	id("build.buildslst")
}

//dependencySettings {
//	includeBuild("dependencies")
//}

rootProject.name = "srskokoro"
