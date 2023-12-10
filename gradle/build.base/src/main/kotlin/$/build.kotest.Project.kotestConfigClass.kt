@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

/**
 * Specify the fully qualified class name to use as Kotest project config.
 *
 * @see io.kotest.core.internal.KotestEngineProperties.configurationClassName
 */
var Project.kotestConfigClass: String?
	get() = extra.let {
		if (it.has(::kotestConfigClass.name)) {
			it[::kotestConfigClass.name] as String?
		} else null
	}
	set(value) {
		extra[::kotestConfigClass.name] = value
	}
