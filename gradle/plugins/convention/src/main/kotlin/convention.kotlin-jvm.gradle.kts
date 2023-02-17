import convention.setUpConvention
import convention.setUpTestTask

plugins {
	id("convention.base")
	kotlin("jvm")
}

kotlin {
	setUpConvention()
}

tasks.test {
	setUpTestTask()
}
