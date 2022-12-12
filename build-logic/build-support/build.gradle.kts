plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
}

repositories {
	mavenCentral()
}

gradlePlugin {
	plugins {
		create("build-support") {
			id = "build-support"
			implementationClass = "BuildSupport"
		}
		create("build-support--kotlin-multiplatform") {
			id = "build-support--kotlin-multiplatform"
			implementationClass = "BuildSupportForKotlinMultiplatform"
		}
	}
}

dependencies {
	compileOnly(kotlin("gradle-plugin", "1.7.10"))
}
