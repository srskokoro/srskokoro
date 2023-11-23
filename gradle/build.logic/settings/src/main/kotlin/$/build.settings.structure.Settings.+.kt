@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.extra
import java.io.File

internal const val gradleProp_structureDirs = "build.settings.structure.dirs"
internal const val gradleProp_structureDirs_root = "$gradleProp_structureDirs.root"

internal const val gradleProp_structureDirs_deps = "$gradleProp_structureDirs.deps"
internal const val gradleProp_structureDirs_conventions = "$gradleProp_structureDirs.conventions"
internal const val gradleProp_structureDirs_plugins = "$gradleProp_structureDirs.plugins"

// --

private const val structureRoot__name = "structureRoot"

val Settings.structureRoot: File
	get() = extensions.let { xs ->
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		xs.findByName(structureRoot__name) as File?
			?: extra.run {
				val settingsDir = settingsDir
				getOrNull<String>(gradleProp_structureDirs_root)?.let {
					val target = File(settingsDir, it).normalize()
					if (target != settingsDir) return@run target
				}
				return@run settingsDir
			}.also {
				xs.add(structureRoot__name, it)
			}
	}

val Settings.isAtStructureRoot: Boolean
	inline get() = settingsDir == structureRoot
