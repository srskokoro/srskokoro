package build.plugins.test

import org.gradle.testfixtures.ProjectBuilder
import java.io.File

fun projectBuilder() = ProjectBuilder.builder().withGradleUserHomeDir(gradleUserHome)

fun projectBuilder(projectDir: File?) = projectBuilder().withProjectDir(projectDir)

inline fun buildProject(configure: ProjectBuilder.() -> Unit = {}) =
	projectBuilder().apply(configure).build()

inline fun buildProject(projectDir: File?, configure: ProjectBuilder.() -> Unit = {}) =
	projectBuilder(projectDir).apply(configure).build()
