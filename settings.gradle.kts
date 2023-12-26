@Suppress("ComplexRedundantLet")
pluginManagement {
	apply(from = "settings.init.gradle.kts")
	val autoGradleProperties: (String) -> String by extra
	autoGradleProperties("gradle/build.base")
	autoGradleProperties("gradle/build.logic").let { includeBuild(it) }
	autoGradleProperties("gradle/conventions").let { includeBuild(it) }
	autoGradleProperties("gradle/plugins").let { includeBuild(it) }
}
plugins {
	id("build.dependencies")
}

val autoGradleProperties: (String) -> String by extra
dependencySettings {
	includeBuild(autoGradleProperties("dependencies"))
}
dependencyResolutionManagement {
	includeBuild(autoGradleProperties("gradle/support"))
}

rootProject.name = "srskokoro"
