plugins {
	id("build.support.kt.jvm")
}

dependencies {
	testImplementation(kotlin("test"))
	testImplementation("com.willowtreeapps.assertk:assertk:0.28.0")

	compileOnly(gradleKotlinDsl())
	testImplementation(gradleKotlinDsl())
}
