package build.api.dsl

import org.gradle.api.Project

inline val Project.isRoot get() = parent == null
