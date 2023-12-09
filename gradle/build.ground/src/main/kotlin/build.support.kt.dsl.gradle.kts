plugins {
	id("build.support.kt.jvm")
	id("org.gradle.kotlin.kotlin-dsl.base")
}

dependencies {
	compileOnly(gradleApi())
	testImplementation(gradleApi())

	compileOnly(gradleKotlinDsl())
	testImplementation(gradleKotlinDsl())
}
