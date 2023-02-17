plugins {
	id("convention.base")
	kotlin("jvm")
}

kotlin {
	jvmToolchain(deps.jvm.toolchainConfig)
}

tasks.test {
	useJUnitPlatform()
}
