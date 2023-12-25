package build.api.dsl.accessors

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

fun DependencyHandler.compileOnlyApiTestImpl(dependencyNotation: Any) {
	compileOnlyApi(dependencyNotation)
	testImplementation(dependencyNotation)
}


fun DependencyHandler.commonMainImplementation(dependencyNotation: Any) =
	add("commonMainImplementation", dependencyNotation)

fun DependencyHandler.commonMainApi(dependencyNotation: Any) =
	add("commonMainApi", dependencyNotation)

fun DependencyHandler.commonMainCompileOnly(dependencyNotation: Any) =
	add("commonMainCompileOnly", dependencyNotation)


fun DependencyHandler.commonTestImplementation(dependencyNotation: Any) =
	add("commonTestImplementation", dependencyNotation)
