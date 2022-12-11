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
	}
}

dependencies {
	compileOnly(kotlin("gradle-plugin", "1.7.10"))
}
