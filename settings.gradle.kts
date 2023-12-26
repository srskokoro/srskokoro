pluginManagement {
	apply(from = "settings.init.gradle.kts")
	val autoGradleProperties: Settings.(String) -> String by extra
	includeBuild(autoGradleProperties("gradle/build.base"))
	includeBuild(autoGradleProperties("gradle/build.logic"))
}
plugins {
	id("build.dependencies")
}
val autoGradleProperties: Settings.(String) -> String by extra

dependencySettings {
	includeBuild(autoGradleProperties("dependencies"))
}

rootProject.name = "srskokoro"
