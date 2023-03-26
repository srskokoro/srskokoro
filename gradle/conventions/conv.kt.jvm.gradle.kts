import conv.internal.setup.*

plugins {
	id("conv.base")
	kotlin("jvm")
}

kotlin {
	setUp(this)
}

tasks.test {
	setUp(this)
}

dependencies {
	setUpTestFrameworkDeps_jvm {
		testImplementation(it)
	}
	setUpTestCommonDeps {
		testImplementation(it)
	}
}
