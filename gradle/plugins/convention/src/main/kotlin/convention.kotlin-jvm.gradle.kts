import convention.setUp
import convention.setUpTestCommonDeps
import convention.setUpTestFrameworkDeps_jvm

plugins {
	id("convention.base")
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
