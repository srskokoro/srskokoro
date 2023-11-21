package conv.deps

import conv.deps.spec.JvmSetupSpec
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.plugins.ExtensionAware
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.jvm.toolchain.JvmVendorSpec

@Suppress("MemberVisibilityCanBePrivate")
abstract class JvmSetup internal constructor(spec: JvmSetupSpec) : ExtensionAware, Action<JavaToolchainSpec> {

	val verLang: JavaLanguageVersion?
	val verObj: JavaVersion?

	val ver: Int = spec.ver.also {
		if (it != 0) {
			verLang = JavaLanguageVersion.of(it)
			verObj = JavaVersion.toVersion(it)
		} else {
			verLang = null
			verObj = null
		}
	}

	val vendor: JvmVendorSpec? = spec.vendor?.value
	val implementation: JvmImplementation? = spec.implementation?.value

	override fun execute(spec: JavaToolchainSpec) {
		spec.languageVersion.set(verLang)
		spec.vendor.set(vendor)
		spec.implementation.set(implementation)
	}
}
