package conv.internal.setup

import conv.internal.util.*
import io.kotest.core.internal.KotestEngineProperties
import kotestConfigClass
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

internal fun Project.setUp(task: Test): Unit = with(task) {
	useJUnitPlatform()
	setUpForDebug(this)

	// Must be 1, as both Kotest and JUnit already have their own mechanism to
	// parallelize tests.
	maxParallelForks = 1
	// ^ WARNING: When greater than 1, the test engine would get forked into
	// several processes, which is quite expensive; it even comes with a heavy
	// startup cost.
	// ^ NOTE: The above is already 1 by default, but this makes things
	// explicit, and also serves as a warning to future devs.

	kotestConfigClass?.let {
		systemProperty(KotestEngineProperties.configurationClassName, it)
		systemProperty(KotestEngineProperties.disableConfigurationClassPathScanning, "true")
	}
}

internal fun setUpTestFrameworkDeps_android(consume: DependencyConsumer) {
	consume("io.kotest:kotest-runner-junit5")
}

internal fun setUpTestFrameworkDeps_jvm(consume: DependencyConsumer) {
	consume("io.kotest:kotest-runner-junit5")
}

internal fun setUpTestFrameworkDeps_kmp_common(consume: DependencyConsumer) {
	consume("io.kotest:kotest-framework-engine")
}

internal fun setUpTestCommonDeps(consume: DependencyConsumer) {
	consume("org.jetbrains.kotlin:kotlin-test") // https://kotlinlang.org/docs/gradle-configure-project.html#set-dependencies-on-test-libraries
	consume("io.kotest:kotest-assertions-core")
}
