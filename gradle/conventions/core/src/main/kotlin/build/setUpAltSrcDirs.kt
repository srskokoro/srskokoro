package build

import build.api.dsl.*
import build.api.dsl.accessors.kotlinSourceSets
import build.api.dsl.accessors.sourceSets
import build.api.extraneousSources
import build.support.from
import build.support.io.getFsSortingPrefixLength
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import java.io.File

internal fun Project.setUpAltSrcDirs() {
	prioritizedAfterEvaluate(Project::setUpAltSrcDirs_)
}

private fun Project.setUpAltSrcDirs_() {
	val src = file("src")
	val possibleAltSrcSetDirs = HashMap<String, String>()

	src.gatherPossibleAltSrcSetDirs(possibleAltSrcSetDirs)
	file(TEST).gatherPossibleAltSrcSetDirs(possibleAltSrcSetDirs)

	val srcPathSlashed = src.path + File.separatorChar
	kotlinSourceSets.configureEach {
		val srcSetName = name
		val isTestSrcSet = isTestSrcSet(srcSetName)

		// NOTE: It may not actually be a directory, but we don't care.
		val altSrcSetDirName: String? = possibleAltSrcSetDirs[srcSetName]

		// Must process first before `kotlin` and `resources`, since `kotlin` or
		// `resources` may include the extraneous source as a nested source
		// directory set. That way, the alternative directories would be
		// associated to the extraneous source first.
		extraneousSources.values.forEach {
			it.setUpAltSrcDirs(altSrcSetDirName, srcSetName, isTestSrcSet, srcPathSlashed)
		}

		kotlin.setUpAltSrcDirs(altSrcSetDirName, srcSetName, isTestSrcSet, srcPathSlashed)
		resources.setUpAltSrcDirs(altSrcSetDirName, srcSetName, isTestSrcSet, srcPathSlashed)
	}

	// Also process any `org.gradle.api.tasks.SourceSet`
	sourceSets.configureEach {
		val srcSetName = name
		val isTestSrcSet = isTestSrcSet(srcSetName)

		// NOTE: It may not actually be a directory, but we don't care.
		val altSrcSetDirName: String? = possibleAltSrcSetDirs[srcSetName]

		java.setUpAltSrcDirs(altSrcSetDirName, srcSetName, isTestSrcSet, srcPathSlashed)
		resources.setUpAltSrcDirs(altSrcSetDirName, srcSetName, isTestSrcSet, srcPathSlashed)
	}
}

private fun File.gatherPossibleAltSrcSetDirs(out: HashMap<String, String>) {
	// NOTE: Let Gradle automatically track the `File.list()` below as
	// configuration input. See also, https://github.com/gradle/gradle/issues/23638
	list()?.forEach { name ->
		getFsSortingPrefixLength(name).let {
			if (it != 0) out[name.from(it)] = name
		}
	}
}

private fun SourceDirectorySet.setUpAltSrcDirs(
	altSrcSetDirName: String?,
	srcSetName: String,
	isTestSrcSet: Boolean,
	srcPathSlashed: String,
) {
	for (srcDir in srcDirs) {
		val p = srcDir.path
		if (!p.startsWith(srcPathSlashed, true)) continue

		var n = srcPathSlashed.length
		if (!p.startsWith(srcSetName, n, true)) continue

		n += srcSetName.length
		if (n >= p.length) continue
		if (p[n] != File.separatorChar) continue

		// NOTE: At this point, `n` should be pointing exactly at the string
		// "/kotlin" or "/resources" or something similar, and that string may
		// even have further sub-paths.

		// With the check performed above, we expect the following to start with
		// a slash -- the system-dependent file separator character.
		val srcDirSubPath = p.from(n)

		if (isTestSrcSet) {
			srcDir(TEST_SLASHED + srcSetName + srcDirSubPath)
			if (altSrcSetDirName == null) continue
			srcDir(TEST_SLASHED + altSrcSetDirName + srcDirSubPath)
		} else {
			if (altSrcSetDirName == null) continue
		}
		srcDir(SRC_SLASHED + altSrcSetDirName + srcDirSubPath)
	}
}

private const val TEST = "test"
private val TEST_SLASHED = TEST + File.separatorChar
private val SRC_SLASHED = "src" + File.separatorChar

private fun isTestSrcSet(name: String): Boolean {
	// Either it's suffixed with "Test" or it's named "test" -- and not because
	// it's suffixed with "test" (all lowercase).
	return name.endsWith("Test") || name == "test"
}
