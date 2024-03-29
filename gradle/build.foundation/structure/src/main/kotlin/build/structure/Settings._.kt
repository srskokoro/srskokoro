package build.structure

import build.api.dsl.*
import build.support.io.safeResolve
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.*
import java.io.File

private fun Settings.getStructureRoot(): File {
	val structureRootValue = extra.getOrElse<Any>("build.structure.root") {
		error("Must set up structure root path via extra property key \"$it\" (or via 'gradle.properties' file)")
	}
	val structureRoot = rootDir.safeResolve(structureRootValue.toString()).canonicalFile
	// Guard against an unintended path set as the structure root
	if (!File(structureRoot, "settings.gradle.kts").exists() && !File(structureRoot, "settings.gradle").exists()) {
		error("Structure root must be a valid build with a Gradle `settings` file (even if empty)" +
			"\n- Structure root: $structureRoot")
	}
	return structureRoot
}

internal fun Settings.include(structure: ProjectStructure) {
	val structureRoot = getStructureRoot()
	structure.findProjects(structureRoot, providers).forEach {
		val id = it.getProjectId(structureRoot)
		include(id) // NOTE: Resolves relative to `Settings.rootDir`
		project(id).projectDir = it.getProjectDir(structureRoot)
	}
}
