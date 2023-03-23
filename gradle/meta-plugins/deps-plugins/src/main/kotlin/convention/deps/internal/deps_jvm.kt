package convention.deps.internal

import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.jvm.toolchain.JvmVendorSpec

@Suppress("ClassName")
interface deps_jvm {

	val toolchainConfig: Action<JavaToolchainSpec>

	val verLang: JavaLanguageVersion

	val verObj: JavaVersion

	val ver: Int

	val vendor: JvmVendorSpec?

	val implementation: JvmImplementation?
}
