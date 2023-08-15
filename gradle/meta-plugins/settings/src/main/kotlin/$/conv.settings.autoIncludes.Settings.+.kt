@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.extra
import java.io.File

internal const val gradleProp_autoIncludesDirs = "conv.settings.auto-includes.dirs"
internal const val gradleProp_autoIncludesDirs_root = "$gradleProp_autoIncludesDirs.root"

private const val autoIncludesRoot__name = "autoIncludesRoot"

val Settings.autoIncludesRoot: File
	get() = extensions.let { xs ->
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		xs.findByName(autoIncludesRoot__name) as File?
			?: extra.run {
				val settingsDir = settingsDir
				if (has(gradleProp_autoIncludesDirs_root)) {
					val v = get(gradleProp_autoIncludesDirs_root) as String
					val target = File(settingsDir, v).normalize()
					if (target != settingsDir) return@run target
				}
				return@run settingsDir
			}.also {
				xs.add(autoIncludesRoot__name, it)
			}
	}

val Settings.isAtAutoIncludesRoot: Boolean
	inline get() = settingsDir == autoIncludesRoot
