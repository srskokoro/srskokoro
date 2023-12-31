package build.api.dsl.accessors

import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

val TaskContainer.test: TaskProvider<Test>
	get() = named<Test>("test")
