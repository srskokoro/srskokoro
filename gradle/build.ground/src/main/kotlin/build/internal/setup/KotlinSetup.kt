package build.internal.setup

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

internal fun Project.setUp() {
	@Suppress("UNCHECKED_CAST")
	val kotlinSourceSets = (extensions.getByName("kotlin") as ExtensionAware)
		.extensions.getByName("sourceSets") as NamedDomainObjectContainer<KotlinSourceSet>

	afterEvaluate { useDollarAsRootPackage(kotlinSourceSets) }
}

private fun Project.useDollarAsRootPackage(kotlinSourceSets: NamedDomainObjectContainer<KotlinSourceSet>) {
	val objects = objects
	val defaultSrcPath = file("src").path + File.separatorChar
	kotlinSourceSets.configureEach {
		val kotlin = kotlin
		// Exclude source files under the `$` package
		kotlin.exclude {
			val segments = it.relativePath.segments
			segments.isNotEmpty() && segments[0] == "$"
				&& it.file.path.startsWith(defaultSrcPath) // Only exclude for source files we control
		}
		// Include source files under the "$" directory as being in the default/root package
		val dollar = objects.sourceDirectorySet("$", "$")
		for (srcDir in kotlin.srcDirs) {
			val path = srcDir.path
			if (!path.startsWith(defaultSrcPath)) continue
			dollar.srcDir(File(path, "$"))
		}
		kotlin.source(dollar)
	}
}
