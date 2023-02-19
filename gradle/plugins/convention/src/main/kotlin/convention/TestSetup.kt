package convention

import org.gradle.api.tasks.testing.Test

internal fun setUp(task: Test): Unit = with(task) {
	useJUnitPlatform()
}

private typealias DependencyConsumer = (dependencyNotation: String) -> Unit

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
