package convention

import org.gradle.api.tasks.testing.Test

internal fun Test.setUpTestTask() {
	useJUnitPlatform()
}

private typealias DependencyConsumer = (dependencyNotation: String) -> Unit

internal fun setUpTestFrameworkDeps_android(consumer: DependencyConsumer) {
}

internal fun setUpTestFrameworkDeps_jvm(consumer: DependencyConsumer) {
}

internal fun setUpTestFrameworkDeps_kmp_common(consumer: DependencyConsumer) {
}

internal fun setUpTestCommonDeps(consumer: DependencyConsumer) {
}
