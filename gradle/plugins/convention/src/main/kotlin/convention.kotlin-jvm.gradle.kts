import convention.configureConvention
import convention.configureTestTask

plugins {
	id("convention.base")
	kotlin("jvm")
}

kotlin {
	configureConvention()
}

tasks.test {
	configureTestTask()
}
