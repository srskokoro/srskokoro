import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmVendorSpec

@Suppress("ClassName", "MemberVisibilityCanBePrivate")
abstract class cfgs_jvm internal constructor() {
	val verInt = 17

	val verObj = JavaVersion.toVersion(verInt)
	val langVer = JavaLanguageVersion.of(verInt)

	@Suppress("UnstableApiUsage")
	val vendor: JvmVendorSpec = JvmVendorSpec.ADOPTIUM

	val toolchainConfig = Action<JavaToolchainSpec> {
		languageVersion.set(langVer)
		vendor.set(this@cfgs_jvm.vendor)
	}

	val kotlinOptTarget = verInt.let {
		if (it <= 8) "1.$it"
		else it.toString()
	}
}
