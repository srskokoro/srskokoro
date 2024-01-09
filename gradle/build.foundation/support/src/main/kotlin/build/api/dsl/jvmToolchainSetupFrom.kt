package build.api.dsl

import org.gradle.api.Action
import org.gradle.api.UnknownDomainObjectException
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.*

fun jvmToolchainSetupFrom(map: Map<String, String>) = Action<JavaToolchainSpec> {
	languageVersion = JavaLanguageVersion.of(map.getOrThrow("jvm.lang").toInt())
	vendor = JvmVendorSpec::class.java.getField(map.getOrThrow("jvm.vendor")).get(null) as JvmVendorSpec
	implementation = JvmImplementation::class.java.getField(map.getOrThrow("jvm.implementation")).get(null) as JvmImplementation
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Map<String, String>.getOrThrow(key: String) =
	get(key) ?: throw E_UnknownPropKey(key)

private fun E_UnknownPropKey(key: String) = UnknownDomainObjectException(
	"Could not find property with key: $key"
)
