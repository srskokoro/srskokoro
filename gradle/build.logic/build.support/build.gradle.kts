plugins {
	id("build.support.kt.jvm")
}

dependencies {
	gradleApi().let { compileOnly(it); testImplementation(it) }
	gradleKotlinDsl().let { compileOnly(it); testImplementation(it) }

	testImplementation(kotlin("test"))
	testImplementation("com.willowtreeapps.assertk:assertk:0.28.0")
}
