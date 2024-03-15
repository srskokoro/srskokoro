package org.gradle.kotlin.dsl

import build.version.InternalVersion
import org.gradle.api.Project

var Project.versionName: String
	inline get() = version.toString()
	inline set(value) {
		version = value
	}

var Project.versionCode: Int
	get() = extra.get(::versionCode.name) as Int
	set(value) = extra.set(::versionCode.name, value)


fun Project.getVersionBaseName(): String =
	versionName.substringBefore(InternalVersion.SUFFIX_START_CHAR)
