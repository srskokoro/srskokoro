plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
}

gradlePlugin {
	plugins {
		register("convention") {
			id = "convention"
			implementationClass = "ConventionPlugin"
		}
		register("convention--kotlin-multiplatform") {
			id = "convention--kotlin-multiplatform"
			implementationClass = "ConventionPluginForKotlinMultiplatform"
		}
	}
}
