package convention.deps.internal

import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.jvm.toolchain.JvmVendorSpec

@Suppress("ClassName")
internal class deps_jvm_spec : deps_jvm {

	private var _toolchainConfig: Action<JavaToolchainSpec>? = null
	override val toolchainConfig: Action<JavaToolchainSpec>
		get() = _toolchainConfig ?: run {
			// NOTE: Fetch needed values in advance, to cause them to throw if
			// not yet initialized. Also caches current values, so that later
			// updates would not cause the captured values to change.
			val languageVersionValue = verLang
			val vendorValue = vendor
			val implementationValue = _implementation

			Action<JavaToolchainSpec> {
				languageVersion.set(languageVersionValue)
				vendor.set(vendorValue)
				implementation.set(implementationValue)
			}
		}.also { _toolchainConfig = it }

	private fun invalidate() {
		_toolchainConfig = null // Cause re-init
	}

	// --

	private lateinit var _verLang: JavaLanguageVersion
	override val verLang get() = _verLang

	private lateinit var _verObj: JavaVersion
	override val verObj get() = _verObj

	private var _ver = 0
	override var ver
		get() = _ver
		internal set(value) {
			// NOTE: Assign first to local variables as the following methods
			// may throw and we don't want fields to have inconsistent states.
			val verLang = JavaLanguageVersion.of(value)
			val verObj = JavaVersion.toVersion(value)
			_ver = value
			_verObj = verObj
			_verLang = verLang
			invalidate()
		}

	private var _vendor: JvmVendorSpec? = null
	override var vendor
		get() = _vendor
		internal set(value) {
			_vendor = value
			invalidate()
		}

	private var _implementation: JvmImplementation? = null
	override var implementation
		get() = _implementation
		internal set(value) {
			_implementation = value
			invalidate()
		}
}
