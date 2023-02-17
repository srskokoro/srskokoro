import convention.setUp
import convention.setUpTestCommonDeps
import convention.setUpTestFrameworkDeps_jvm
import convention.setUpTestTask

plugins {
	id("convention.base")
	kotlin("jvm")
}

kotlin {
	setUp(this)
}

tasks.test {
	setUpTestTask()
}

dependencies {
	setUpTestFrameworkDeps_jvm {
		testImplementation(it)
	}
	setUpTestCommonDeps {
		testImplementation(it)
	}
}
