package conv.internal.setup

import conv.internal.KotlinTargetsConfigLoader
import org.gradle.api.Action
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

	// Allow tests to be placed under 'test' directory instead of 'src'
	kotlinSourceSets.configureEach(object : Action<KotlinSourceSet> {
		private val defaultSrcPath = file("src").path + File.separatorChar
		private val separateTestDir = file("test")

		private fun SourceDirectorySet.setUpSeparateTestDir() {
			srcDirs.forEach {
				val path = it.path
				if (path.startsWith(defaultSrcPath)) srcDir(File(
					separateTestDir, path.substring(defaultSrcPath.length),
				))
			}
		}

		override fun execute(srcSet: KotlinSourceSet) {
			if (srcSet.name.endsWith("Test")) {
				srcSet.kotlin.setUpSeparateTestDir()
				srcSet.resources.setUpSeparateTestDir()
			}
		}
	})
}
