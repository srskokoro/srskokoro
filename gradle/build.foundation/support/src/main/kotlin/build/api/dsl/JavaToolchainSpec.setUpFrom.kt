package build.api.dsl

import org.gradle.api.UnknownDomainObjectException
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.*

fun JavaToolchainSpec.setUpFrom(map: Map<String, String>) {
	languageVersion = JavaLanguageVersion.of(map.getOrThrow("jvm.lang").toInt())
	vendor = JvmVendorSpec::class.java.getField(map.getOrThrow("jvm.vendor")).get(null) as JvmVendorSpec
	implementation = JvmImplementation::class.java.getField(map.getOrThrow("jvm.implementation")).get(null) as JvmImplementation
}

/**
 * WARNING: Assumes that [languageVersion][JavaToolchainSpec.getLanguageVersion]
 * already has a value. This is a no-op otherwise.
 */
fun JavaToolchainSpec.restrictVersionForBuildInclusive() {
	languageVersion.run {
		val value = orNull ?: return
		val maxVersion = Runtime.version().feature()
		if (value.asInt() > maxVersion)
			set(JavaLanguageVersion.of(maxVersion))
	}
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Map<String, String>.getOrThrow(key: String) =
	get(key) ?: throw E_UnknownPropKey(key)

private fun E_UnknownPropKey(key: String) = UnknownDomainObjectException(
	"Could not find property with key: $key"
)
