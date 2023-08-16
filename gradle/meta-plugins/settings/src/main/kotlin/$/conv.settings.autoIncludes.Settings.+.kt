@file:Suppress("PackageDirectoryMismatch")

import conv.internal.support.dsl.getOrNull
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.extra
import java.io.File

internal const val gradleProp_autoIncludesDirs = "conv.settings.auto-includes.dirs"
internal const val gradleProp_autoIncludesDirs_root = "$gradleProp_autoIncludesDirs.root"

internal const val gradleProp_autoIncludesDirs_deps = "$gradleProp_autoIncludesDirs.deps"
internal const val gradleProp_autoIncludesDirs_plugins = "$gradleProp_autoIncludesDirs.plugins"

// --

private const val autoIncludesRoot__name = "autoIncludesRoot"

val Settings.autoIncludesRoot: File
	get() = extensions.let { xs ->
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		xs.findByName(autoIncludesRoot__name) as File?
			?: extra.run {
				val settingsDir = settingsDir
				getOrNull<String>(gradleProp_autoIncludesDirs_root)?.let {
					val target = File(settingsDir, it).normalize()
					if (target != settingsDir) return@run target
				}
				return@run settingsDir
			}.also {
				xs.add(autoIncludesRoot__name, it)
			}
	}

val Settings.isAtAutoIncludesRoot: Boolean
	inline get() = settingsDir == autoIncludesRoot
