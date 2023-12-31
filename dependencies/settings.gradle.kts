pluginManagement {
	includeBuild("../gradle/build.support")
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

	// See also, https://youtrack.jetbrains.com/issue/KT-54691#focus=Comments-27-6852272.0-0
	moduleKotlin("gradle-plugins-bom", kotlin)
	moduleKotlin("gradle-plugin", kotlin)

	pluginKotlin("plugin.assignment", kotlin)
	moduleKotlin("assignment", kotlin)
	pluginKotlin("plugin.sam.with.receiver", kotlin)
	moduleKotlin("sam-with-receiver", kotlin)

	// See, https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	moduleKotlin("bom", kotlin)
	moduleKotlin("stdlib", kotlin)
	moduleKotlin("reflect", kotlin)
	moduleKotlin("test", kotlin)
	moduleKotlin("test-junit5", kotlin)
}
