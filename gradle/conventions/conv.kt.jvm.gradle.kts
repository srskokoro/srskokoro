import conv.internal.setup.*

plugins {
	id("conv.base")
	kotlin("jvm")
}

kotlin {
	setUp(this)
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
