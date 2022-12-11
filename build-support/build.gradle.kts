plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly(kotlin("gradle-plugin"))
}

gradlePlugin {
	plugins {
		create("build-support") {
			id = "build-support"
			implementationClass = "BuildSupport"
		}
	}
}
