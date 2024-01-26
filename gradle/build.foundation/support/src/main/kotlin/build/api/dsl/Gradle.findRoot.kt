package build.api.dsl

import org.gradle.api.invocation.Gradle

fun Gradle.findRoot(): Gradle {
	var gradle = this
	var parent = gradle.parent

	while (parent != null) {
		gradle = parent
		parent = gradle.parent
	}

	return gradle
}
