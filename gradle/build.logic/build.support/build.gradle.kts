plugins {
	id("build.support.kt.jvm")
}

dependencies {
	compileOnly(gradleApi())
	testImplementation(gradleApi())
	compileOnly(gradleKotlinDsl())
	testImplementation(gradleKotlinDsl())

	testImplementation(kotlin("test"))
	testImplementation("com.willowtreeapps.assertk:assertk:0.28.0")
}
