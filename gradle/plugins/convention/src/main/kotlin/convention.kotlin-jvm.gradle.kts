import convention.configureConvention

plugins {
	id("convention.base")
	kotlin("jvm")
}

kotlin {
	configureConvention()
}

tasks.test {
	useJUnitPlatform()
}
