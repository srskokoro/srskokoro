package build.api.dsl.model

import build.api.dsl.*
import org.gradle.api.NamedDomainObjectContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

val KotlinProjectExtension.kotlinSourceSets: NamedDomainObjectContainer<KotlinSourceSet>
	get() = x("sourceSets")
