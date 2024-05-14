package build.api.dsl

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory

inline fun ObjectFactory.sourceDirectorySet(
	name: String,
	displayName: String = name,
	configure: (SourceDirectorySet) -> Unit = {},
): SourceDirectorySet = sourceDirectorySet(name, displayName).also(configure)
