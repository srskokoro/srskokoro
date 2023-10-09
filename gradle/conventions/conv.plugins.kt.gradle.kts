import conv.internal.setup.*

plugins {
	id("conv.plugins.base")
	`java-gradle-plugin`
	kotlin("jvm")
	id("build.plugins.kt")
}

kotlin {
	kotlinSourceSets = getSourceSets(this)
}

tasks.test {
	setUp(this)
}

dependencies {
	// https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	platform("org.jetbrains.kotlin:kotlin-bom").let { bom ->
		implementation(bom)
		testImplementation(bom)
	}

	setUpTestFrameworkDeps_jvm {
		testImplementation(it)
	}
	setUpTestCommonDeps {
		testImplementation(it)
	}
}
