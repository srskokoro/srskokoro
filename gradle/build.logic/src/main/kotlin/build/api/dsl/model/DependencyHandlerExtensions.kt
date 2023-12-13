package build.api.dsl.model

import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.implementation(dependencyNotation: Any) =
	add("implementation", dependencyNotation)

fun DependencyHandler.api(dependencyNotation: Any) =
	add("api", dependencyNotation)

fun DependencyHandler.compileOnly(dependencyNotation: Any) =
	add("compileOnly", dependencyNotation)

fun DependencyHandler.compileOnlyApi(dependencyNotation: Any) =
	add("compileOnlyApi", dependencyNotation)


fun DependencyHandler.testImplementation(dependencyNotation: Any) =
	add("testImplementation", dependencyNotation)


fun DependencyHandler.compileOnlyTestImpl(dependencyNotation: Any) {
	compileOnly(dependencyNotation)
	testImplementation(dependencyNotation)
}
