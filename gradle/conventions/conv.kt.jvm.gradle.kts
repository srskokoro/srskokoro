import conv.internal.setup.*
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
	id("conv.base")
	kotlin("jvm")
}

kotlin {
	setUp(this)
}

tasks.withType<KotlinJvmCompile>().configureEach {
	setUp(compilerOptions)
}

tasks.test {
	setUp(this)
}

dependencies {
	// https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

	setUpTestFrameworkDeps_jvm {
		testImplementation(it)
	}
	setUpTestCommonDeps {
		testImplementation(it)
	}
}
