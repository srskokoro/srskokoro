@file:Suppress("PackageDirectoryMismatch")

import build.support.gradle.getOrNull
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

/**
 * Specify the fully qualified class name to use as Kotest project config.
 *
 * @see io.kotest.core.internal.KotestEngineProperties.configurationClassName
 */
var Project.kotestConfigClass: String?
	get() = extra.getOrNull(::kotestConfigClass.name)
	set(value) = extra.set(::kotestConfigClass.name, value)
