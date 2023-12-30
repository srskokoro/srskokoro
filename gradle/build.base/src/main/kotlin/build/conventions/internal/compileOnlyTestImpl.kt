package build.conventions.internal

import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.compileOnlyTestImpl(dependencyNotation: Any) {
	add("compileOnly", dependencyNotation)
	add("testImplementation", dependencyNotation)
}
