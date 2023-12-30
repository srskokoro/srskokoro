pluginManagement {
	includeBuild("../gradle/build.logic")
}
plugins {
	id("build.dependencies")
}

dependencySettings {
	exportOnly()

	/** Should correspond to [JavaToolchainSpec] */
	run<Unit> {
		prop("jvm.lang", 21)
		prop("jvm.vendor", JvmVendorSpec.ADOPTIUM)
	}

	val kotlin = "1.9.21" // https://kotlinlang.org/docs/releases.html
	pluginKotlin("android", kotlin)
	pluginKotlin("jvm", kotlin)
	pluginKotlin("multiplatform", kotlin)
	moduleKotlin("gradle-plugin", kotlin)
	// See also, https://youtrack.jetbrains.com/issue/KT-54691#focus=Comments-27-6852272.0-0
	moduleKotlin("gradle-plugins-bom", kotlin)
	// See, https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	moduleKotlin("bom", kotlin)
	moduleKotlin("test", kotlin)
}
