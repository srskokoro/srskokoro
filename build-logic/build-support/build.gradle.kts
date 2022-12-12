plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
}

repositories {
	mavenCentral()
}

gradlePlugin {
	plugins {
		create("convention") {
			id = "convention"
			implementationClass = "ConventionPlugin"
		}
		create("convention--kotlin-multiplatform") {
			id = "convention--kotlin-multiplatform"
			implementationClass = "ConventionPluginForKotlinMultiplatform"
		}
	}
}

dependencies {
	compileOnly(kotlin("gradle-plugin", "1.7.10"))
}
