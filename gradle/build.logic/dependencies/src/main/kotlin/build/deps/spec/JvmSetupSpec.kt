package build.deps.spec

import build.deps.JvmSetupImplementation
import build.deps.JvmSetupVendor
import org.gradle.api.plugins.ExtensionAware

abstract class JvmSetupSpec internal constructor() : ExtensionAware {
	var ver: Int = 0

	var vendor: JvmSetupVendor? = null
	inline fun vendor(crossinline supplier: JvmSetupVendor.Companion.() -> JvmSetupVendor?) {
		vendor = supplier(JvmSetupVendor.Companion)
	}

	var implementation: JvmSetupImplementation? = null
	inline fun implementation(crossinline supplier: JvmSetupImplementation.Companion.() -> JvmSetupImplementation?) {
		implementation = supplier(JvmSetupImplementation.Companion)
	}
}
