package build.api

import org.gradle.api.Plugin

abstract class UtilityOnly<T : Any> : Plugin<T> {

	final override fun apply(target: T) {
		// Do nothing. The purpose of this plugin is to simply bring in some
		// useful utilities to the buildscript classpath.
	}
}