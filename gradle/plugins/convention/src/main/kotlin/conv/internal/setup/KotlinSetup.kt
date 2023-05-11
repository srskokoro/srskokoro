package conv.internal.setup

import conv.internal.KotlinTargetsConfigLoader
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.typeOf
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

internal fun Project.setUp(kotlin: KotlinProjectExtension): Unit = with(kotlin) {
	setUpProject(kotlin)
	jvmToolchain(deps.jvm)
}

internal fun Project.setUpTargetsExtensions(kotlin: KotlinMultiplatformExtension) {
	// The following makes sure that the type-safe accessors for extensions
	// added to `targets` are generated. See also, "Understanding when type-safe
	// model accessors are available | Gradle Kotlin DSL Primer | 7.5.1" --
	// https://docs.gradle.org/7.5.1/userguide/kotlin_dsl.html#kotdsl:accessor_applicability
	//
	// NOTE: If one day, this would cause an exception due to "targets" already
	// existing, simply remove the following. It's likely that it's already
	// implemented for us, and if so, we shouldn't need to do anything.
	(kotlin as ExtensionAware).extensions.add(typeOf(), "targets", kotlin.targets)

	val config = layout.projectDirectory.file("build.targets.txt")
	KotlinTargetsConfigLoader(providers, config).loadInto(kotlin)
}

// --

private fun Project.setUpProject(kotlin: KotlinProjectExtension) {
	val kotlinSourceSets = getSourceSets(kotlin)
	this.kotlinSourceSets = kotlinSourceSets
	setUpSeparateTestDir(kotlinSourceSets)
}

/**
 * Allow tests to be placed under 'test' directory instead of 'src'
 */
private fun Project.setUpSeparateTestDir(kotlinSourceSets: NamedDomainObjectContainer<KotlinSourceSet>) {
	val defaultSrcPath = file("src").path + File.separatorChar
	val separateTestDir = file("test")

	fun SourceDirectorySet.setUpSeparateTestDir(): Unit = srcDirs.forEach {
		val path = it.path
		if (path.startsWith(defaultSrcPath)) srcDir(File(
			separateTestDir, path.substring(defaultSrcPath.length),
		))
	}

	// Eager configuration is necessary here (i.e., must use `all` instead of
	// `configureEach`). Otherwise, expect-actual declarations won't work.
	kotlinSourceSets.all {
		// Either it's suffixed with "Test" or it's named "test" (and not because it's suffixed with "test")
		if (name.let { it.endsWith("Test") || it == "test" }) {
			kotlin.setUpSeparateTestDir()
			resources.setUpSeparateTestDir()
		}
	}

	// Also set up for `org.gradle.api.tasks.SourceSet` (if any).
	sourceSets.configureEach {
		// Either it's suffixed with "Test" or it's named "test" (and not because it's suffixed with "test")
		if (name.let { it.endsWith("Test") || it == "test" }) {
			java.setUpSeparateTestDir()
			resources.setUpSeparateTestDir()
		}
	}
}
