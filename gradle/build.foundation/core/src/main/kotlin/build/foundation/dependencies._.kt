package build.foundation

import org.gradle.api.artifacts.dsl.DependencyHandler

@InternalApi
fun DependencyHandler.compileOnlyTestImpl(dependencyNotation: Any) {
	add("compileOnly", dependencyNotation)
	add("testImplementation", dependencyNotation)
}
