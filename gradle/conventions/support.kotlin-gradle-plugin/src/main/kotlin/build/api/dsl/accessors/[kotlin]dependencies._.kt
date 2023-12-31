package build.api.dsl.accessors

import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.commonMainImplementation(dependencyNotation: Any) =
	add("commonMainImplementation", dependencyNotation)

fun DependencyHandler.commonMainApi(dependencyNotation: Any) =
	add("commonMainApi", dependencyNotation)

fun DependencyHandler.commonMainCompileOnly(dependencyNotation: Any) =
	add("commonMainCompileOnly", dependencyNotation)


fun DependencyHandler.commonTestImplementation(dependencyNotation: Any) =
	add("commonTestImplementation", dependencyNotation)
