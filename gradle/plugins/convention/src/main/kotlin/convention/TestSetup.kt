package convention

import org.gradle.api.tasks.testing.Test

internal fun setUp(task: Test): Unit = with(task) {
	useJUnitPlatform()
}

private typealias DependencyConsumer = (dependencyNotation: String) -> Unit

internal fun setUpTestFrameworkDeps_android(consumer: DependencyConsumer) {
	consumer("io.kotest:kotest-runner-junit5")
}

internal fun setUpTestFrameworkDeps_jvm(consumer: DependencyConsumer) {
	consumer("io.kotest:kotest-runner-junit5")
}

internal fun setUpTestFrameworkDeps_kmp_common(consumer: DependencyConsumer) {
	consumer("io.kotest:kotest-framework-engine")
}

internal fun setUpTestCommonDeps(consumer: DependencyConsumer) {
	consumer("org.jetbrains.kotlin:kotlin-test")
	consumer("io.kotest:kotest-assertions-core")
}
