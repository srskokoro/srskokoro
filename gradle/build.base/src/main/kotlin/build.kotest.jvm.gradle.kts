import build.kotest.setUp
import build.kotest.setUpTestDependencies

plugins {
	kotlin("jvm")
}

tasks.test.configure(Test::setUp)

dependencies {
	setUpTestDependencies(::testImplementation)
	testImplementation("io.kotest:kotest-runner-junit5")
}
