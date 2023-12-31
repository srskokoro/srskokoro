package build.api

import org.gradle.api.Plugin

abstract class UtilityPlugin(
	private val apply: Any.() -> Unit = {
		// The primary purpose of this plugin is to simply bring in some useful
		// utilities to the buildscript classpath. Thus, by default, do nothing.
	},
) : Plugin<Any> {
	override fun apply(target: Any) {
		apply.invoke(target)
	}
}
