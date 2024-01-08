package build.foundation

import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import kotlin.reflect.KFunction2

fun BuildFoundation.setUpMppLibTargets(project: Project): Unit = with(project) {
	with(extensions.getByName("kotlin") as KotlinMultiplatformExtension) {
		// Apply this now (instead of waiting for it to be applied later), so
		// that Gradle may generate type-safe model accessors for the default
		// hierarchy.
		applyDefaultHierarchyTemplate()

		// -=-
		// Should support at best, targets that we can test with Kotest
		// (which is version 5.8 at the time of writing). See,
		// - https://github.com/kotest/kotest/blob/v5.8.0/buildSrc/src/main/kotlin/kotest-js-conventions.gradle.kts
		// - https://github.com/kotest/kotest/blob/v5.8.0/buildSrc/src/main/kotlin/kotest-native-conventions.gradle.kts

		jvm()

		if (project.extra.parseBoolean("BUILD_KJS", true)) {
			js(IR) {
				browser()
				nodejs()
			}
		} else {
			configurations.registerDummyConfigurations("js")
		}

		val s = KotlinTargetSetup()
		run<KotlinTargetSetup> {
			if (project.extra.parseBoolean("BUILD_KN", true)) s
			else KotlinTargetSetup.Dummy(configurations)
		}.let { x ->
			x(::iosX64)
			x(::iosArm64)
			x(::iosSimulatorArm64)

			x(::tvosX64)
			x(::tvosArm64)
			x(::tvosSimulatorArm64)

			x(::watchosX64)
			x(::watchosArm32)
			x(::watchosArm64)
//			x(::watchosDeviceArm64)
			x(::watchosSimulatorArm64)

			// See, https://stackoverflow.com/a/31443955
			val os = OperatingSystem.current()
			(if (os.isLinux) s else x).also { w ->
				w(::linuxX64)
				w(::linuxArm64)
			}
			(if (os.isMacOsX) s else x).also { w ->
				w(::macosX64)
				w(::macosArm64)
			}
			(if (os.isWindows) s else x).also { w ->
				w(::mingwX64)
			}
		}
	}
}

private open class KotlinTargetSetup {

	operator fun <T : KotlinTarget> invoke(fn: KFunction2<String, T.() -> Unit, T>, configure: T.() -> Unit = {}) = invoke(fn, fn.name, configure)

	open operator fun <T : KotlinTarget> invoke(fn: KFunction2<String, T.() -> Unit, T>, name: String, configure: T.() -> Unit = {}) {
		fn.invoke(name, configure)
	}

	open class Dummy(val configurations: ConfigurationContainer) : KotlinTargetSetup() {

		override fun <T : KotlinTarget> invoke(fn: KFunction2<String, T.() -> Unit, T>, name: String, configure: T.() -> Unit) {
			configurations.registerDummyConfigurations(name)
		}
	}
}

private fun ConfigurationContainer.registerDummyConfigurations(name: String) {
	register("${name}MainApi")
	register("${name}MainImplementation")
	register("${name}MainCompileOnly")
	register("${name}MainRuntimeOnly")
	register("${name}TestApi")
	register("${name}TestImplementation")
	register("${name}TestCompileOnly")
	register("${name}TestRuntimeOnly")
}
