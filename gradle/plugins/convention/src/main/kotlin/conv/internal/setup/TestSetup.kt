package conv.internal.setup

import conv.internal.util.*
import io.kotest.core.internal.KotestEngineProperties
import kotestConfigClass
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

internal fun Project.setUp(task: Test): Unit = with(task) {
	useJUnitPlatform()
	setUpForDebug(this)

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
	consume("org.jetbrains.kotlin:kotlin-test")
	consume("io.kotest:kotest-assertions-core")
}
