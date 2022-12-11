plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly(kotlin("gradle-plugin", "1.7.10"))
}

gradlePlugin {
	plugins {
		create("build-support") {
			id = "build-support"
			implementationClass = "BuildSupport"
		}
	}
}
