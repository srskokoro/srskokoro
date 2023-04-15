@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

var Project.versionName: String
	inline get() = version.toString()
	inline set(value) = run<Unit> { version = value }

var Project.versionCode: Int
	get() = extra[::versionCode.name] as Int
	set(value) = run<Unit> { extra[::versionCode.name] = value }
