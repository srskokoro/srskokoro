plugins {
	id("conv.root")

	// Avoid the plugins to be loaded multiple times in each subproject's
	// classloader. See also, https://youtrack.jetbrains.com/issue/KT-46200
	kotlin("multiplatform") apply false
	kotlin("android") apply false
	kotlin("jvm") apply false
	kotlin("js") apply false
	id("com.android.application") apply false
	id("com.android.library") apply false
	id("io.kotest.multiplatform") apply false
	id("app.cash.redwood") apply false

	id("com.louiscad.complete-kotlin")
}

// KLUDGE Can't yet exclude "testRelease" tasks via the `-x` CLI option.
//  - Issue since Gradle 7.6: https://github.com/gradle/gradle/issues/24341
//  - See also, https://github.com/gradle/gradle/pull/25214
if (
	gradle.startParameter.taskNames.any {
		when (it) {
			"check" -> true
			"test" -> true
			"allTests" -> true
			else -> false
		}
	}
) allprojects(fun Project.() = afterEvaluate(fun Project.() = tasks.all(fun(task: Task) {
	if (task.name.contains("testRelease", ignoreCase = true)) {
		// The following attempts to simulate the `-x` Gradle CLI option
		task.onlyIf { false } // Prevents task execution
		task.setDependsOn(emptyList<Any?>()) // Clears task dependencies
	}
})))

// NOTE: Only modify the `group` for direct subprojects of this project; let
// Gradle automatically provide a unique `group` for subprojects of subprojects.
// - See also, https://github.com/gradle/gradle/issues/847#issuecomment-1205001575
childProjects.values.forEach {
	it.group = "srs.kokoro"
}
