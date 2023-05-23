plugins {
	// Necessary to avoid the plugin to be loaded multiple times in each
	// subproject's classloader -- https://youtrack.jetbrains.com/issue/KT-46200
	`kotlin-dsl` apply false
}

subprojects {
	pluginManager.withPlugin("org.gradle.kotlin.kotlin-dsl") {
		project.extra["kotlin.jvm.target.validation.mode"] = "ignore"
		tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
			compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
		}
	}
}

allprojects {
	group = "convention"
}
