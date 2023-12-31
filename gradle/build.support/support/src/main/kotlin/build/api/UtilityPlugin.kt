package build.api

import org.gradle.api.Plugin

abstract class UtilityPlugin : Plugin<Any> {

	override fun apply(target: Any) {
		// Do nothing. The purpose of this plugin is to simply bring in some
		// useful utilities to the buildscript classpath.
	}
}
