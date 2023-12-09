plugins {
	id("build.support.kt.dsl")
}

dependencies {
	testImplementation(kotlin("test"))
	testImplementation("com.willowtreeapps.assertk:assertk:0.28.0")
}
