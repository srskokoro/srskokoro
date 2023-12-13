package build.api.dsl.model

import build.api.dsl.*
import org.gradle.api.Project
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

val Project.gradlePlugin: GradlePluginDevelopmentExtension
	get() = x("gradlePlugin")


val Project.kotlin: KotlinProjectExtension
	get() = x("kotlin")

val Project.kotlinJvm: KotlinJvmProjectExtension
	get() = x("kotlin")
