plugins {
	id("build.support.kt.jvm")
	id("org.gradle.kotlin.kotlin-dsl.base")
}

// NOTE: We explicitly depend on the following because... paranoia
dependencies {
	implementation(gradleApi())
	testImplementation(gradleApi())
	implementation(gradleKotlinDsl())
	testImplementation(gradleKotlinDsl())
}
