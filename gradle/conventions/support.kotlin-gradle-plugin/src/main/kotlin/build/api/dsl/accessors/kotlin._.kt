package build.api.dsl.accessors

import build.api.dsl.*
import org.gradle.api.NamedDomainObjectContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

val KotlinTopLevelExtension.kotlinSourceSets: NamedDomainObjectContainer<KotlinSourceSet>
	get() = x("sourceSets")
