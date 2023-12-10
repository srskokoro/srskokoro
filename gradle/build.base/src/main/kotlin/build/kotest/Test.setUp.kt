package build.kotest

import ASSERTK_VERSION
import KOTEST_VERSION
import io.kotest.core.internal.KotestEngineProperties
import kotestConfigClass
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.kotlin

internal fun Test.setUp() {
	useJUnitPlatform()
	jvmArgs("-ea")

	// Set up properties to significantly improve test startup time!
	systemProperty(KotestEngineProperties.disableAutoScanClassPathScanning, "true")
	systemProperty(KotestEngineProperties.disableConfigurationClassPathScanning, "true")
	systemProperty(KotestEngineProperties.disableJarDiscovery, "true")

	project.kotestConfigClass?.let {
		systemProperty(KotestEngineProperties.configurationClassName, it)
	}

	// Must be 1, as both Kotest and JUnit already have their own mechanism to
	// parallelize tests.
	maxParallelForks = 1
	// ^ WARNING: When greater than 1, the test engine would get forked into
	// several processes, which is quite expensive; it even comes with a heavy
	// startup cost.
	// ^ NOTE: The above is already 1 by default, but this makes things
	// explicit, and also serves as a warning to future devs.
}

internal inline fun Project.setUpTestDependencies(via: (dependencyNotation: Any) -> Unit) {
	val d = dependencies
	via.invoke(d.kotlin("test"))
	via.invoke(d.platform("io.kotest:kotest-bom:$KOTEST_VERSION"))
	via.invoke("io.kotest:kotest-framework-api")
	via.invoke("com.willowtreeapps.assertk:assertk:$ASSERTK_VERSION")
}
