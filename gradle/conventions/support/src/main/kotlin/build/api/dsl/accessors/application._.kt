package build.api.dsl.accessors

import build.api.dsl.*
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.provider.ListProperty

/**
 * NOTE: This is expected to be set up by our JVM convention plugin.
 */
val JavaApplication.jvmArgs: ListProperty<String>
	get() = x("jvmArgs")
