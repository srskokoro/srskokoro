plugins {
	id("build.support.kt.jvm")
	id("org.gradle.kotlin.kotlin-dsl.base")
}

dependencies {
	gradleApi().let { compileOnly(it); testImplementation(it) }
	gradleKotlinDsl().let { compileOnly(it); testImplementation(it) }
}
