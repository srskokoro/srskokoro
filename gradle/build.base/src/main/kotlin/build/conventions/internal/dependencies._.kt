package build.conventions.internal

import org.gradle.api.artifacts.dsl.DependencyHandler

@InternalConventionsApi
fun DependencyHandler.compileOnlyTestImpl(dependencyNotation: Any) {
	add("compileOnly", dependencyNotation)
	add("testImplementation", dependencyNotation)
}

internal fun DependencyHandler.commonMainCompileOnlyTestImpl(dependencyNotation: Any) {
	add("commonMainCompileOnly", dependencyNotation)
	add("commonTestImplementation", dependencyNotation)
}