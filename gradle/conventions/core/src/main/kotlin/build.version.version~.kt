package org.gradle.kotlin.dsl

import org.gradle.api.Project

var Project.versionName: String
	inline get() = version.toString()
	inline set(value) {
		version = value
	}

var Project.versionCode: Int
	get() = extra.get(::versionCode.name) as Int
	set(value) = extra.set(::versionCode.name, value)
