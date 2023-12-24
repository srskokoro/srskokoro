package build.api.dsl.accessors

import build.api.dsl.*
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

val Project.gradlePlugin: GradlePluginDevelopmentExtension
	get() = x("gradlePlugin")


val Project.kotlin: KotlinProjectExtension
	get() = x("kotlin")

val Project.kotlinJvm: KotlinJvmProjectExtension
	get() = x("kotlin")

val Project.kotlinMpp: KotlinMultiplatformExtension
	get() = x("kotlin")


/** @see build.kt.base._plugin */
val Project.kotlinSourceSets: NamedDomainObjectContainer<KotlinSourceSet>
	get() = x("kotlinSourceSets")
