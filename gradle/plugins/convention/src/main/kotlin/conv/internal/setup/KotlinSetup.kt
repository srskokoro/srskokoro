package conv.internal.setup

import assets
import conv.internal.KotlinTargetsConfigLoader
import conv.internal.skipPlaceholderGenerationForKotlinTargetsConfigLoader
import conv.internal.support.removeFirst
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
	KotlinTargetsConfigLoader(providers, config).loadInto(kotlin, skipPlaceholderGenerationForKotlinTargetsConfigLoader)
}

// --

private fun Project.setUpProject(kotlin: KotlinProjectExtension) {
	val kotlinSourceSets = getSourceSets(kotlin)
	this.kotlinSourceSets = kotlinSourceSets
	setUpAltSrcDirs(kotlinSourceSets)
}

private fun Project.setUpAltSrcDirs(kotlinSourceSets: NamedDomainObjectContainer<KotlinSourceSet>) {
	val defaultSrcPath = file("src").path + File.separatorChar

	kotlinSourceSets.configureEach {
		val isTestSourceSet = isTestSourceSet(name)
		kotlin.setUpAltSrcDirs(defaultSrcPath, isTestSourceSet)
		resources.setUpAltSrcDirs(defaultSrcPath, isTestSourceSet)
		assets?.setUpAltSrcDirs(defaultSrcPath, isTestSourceSet)
	}

	// Also set up for `org.gradle.api.tasks.SourceSet` (if any).
	sourceSets.configureEach {
		val isTestSourceSet = isTestSourceSet(name)
		java.setUpAltSrcDirs(defaultSrcPath, isTestSourceSet)
		resources.setUpAltSrcDirs(defaultSrcPath, isTestSourceSet)
	}
}

/**
 * NOTE: The parameter [defaultSrcPath] must be a value evaluated via the
 * following expression (or equivalent):
 * ```
 * project.file("src").path + File.separatorChar
 * ```
 *
 * @see Project.file
 * @see File.separatorChar
 */
private fun SourceDirectorySet.setUpAltSrcDirs(
	defaultSrcPath: String,
	isTestSourceSet: Int,
): Unit = srcDirs.forEach { srcDir ->
	val path = srcDir.path
	if (path.startsWith(defaultSrcPath)) {
		val subPath = path.removeFirst(defaultSrcPath.length)
		srcDir("src" + File.separatorChar + "#" + subPath)
		srcDir("src" + File.separatorChar + "+" + subPath)
		if (isTestSourceSet != 0) {
			srcDir("test" + File.separatorChar + subPath)
			srcDir("test" + File.separatorChar + "#" + subPath)
			srcDir("test" + File.separatorChar + "+" + subPath)
			if (isTestSourceSet == 1) srcDir(subPath)
		}
	}
}

private fun isTestSourceSet(name: String): Int {
	// Either it's suffixed with "Test" or it's named "test" -- and not because
	// it's suffixed with "test" (all lowercase).
	return if (!name.endsWith("Test")) {
		if (name != "test") 0 else 1
	} else 2
}
