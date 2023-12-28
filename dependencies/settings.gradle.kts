pluginManagement {
	includeBuild("../gradle/build.logic")
}
plugins {
	id("build.dependencies")
}

dependencySettings {
	exportOnly()

	val kotlin = "1.9.21" // https://kotlinlang.org/docs/releases.html
	pluginKotlin("android", kotlin)
	pluginKotlin("jvm", kotlin)
	pluginKotlin("multiplatform", kotlin)
	moduleKotlin("gradle-plugin", kotlin)
}
