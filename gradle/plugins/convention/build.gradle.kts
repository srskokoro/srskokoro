plugins {
	`kotlin-dsl`
}

dependencies {
	implementation("convention:deps-plugins")

	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
	implementation("com.android.tools.build:gradle")

	implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle")
}
