package build.plugins.test

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

fun projectBuilder(): ProjectBuilder = ProjectBuilder.builder().withGradleUserHomeDir(gradleUserHome)

fun projectBuilder(projectDir: File?): ProjectBuilder = projectBuilder().withProjectDir(projectDir)

inline fun buildProject(configure: ProjectBuilder.() -> Unit = {}): Project =
	projectBuilder().apply(configure).build()

inline fun buildProject(projectDir: File?, configure: ProjectBuilder.() -> Unit = {}): Project =
	projectBuilder(projectDir).apply(configure).build()
