package build.plugins.base

import build.api.addExtraneousSourceTo
import build.api.dsl.accessors.gradlePlugin
import build.plugins.base.PluginsAutoRegistrant.Companion.PLUGIN_CLASS
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.EmptyFileVisitor
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * @see PluginsAutoRegistrant
 */
internal fun installPluginsAutoRegistrant(main: KotlinSourceSet): Unit = main.project.run {
	val pluginsDir = file("src/${main.name}/plugins")
	main.kotlin.source(objects.addExtraneousSourceTo(main, "plugins").apply {
		srcDir(pluginsDir)
		include("**/$PLUGIN_CLASS.kt")
	})
	val gradlePlugins = gradlePlugin.plugins
	providers.of(PluginsAutoRegistrant::class.java) {
		parameters.pluginsDir.set(pluginsDir)
	}.get().forEach {
		gradlePlugins.create(it) {
			id = it
			implementationClass = "$it.$PLUGIN_CLASS"
		}
	}
}

internal abstract class PluginsAutoRegistrant : ValueSource<Set<String>, PluginsAutoRegistrant.Parameters> {

	companion object {
		const val PLUGIN_CLASS = "_plugin"
	}

	interface Parameters : ValueSourceParameters {
		val pluginsDir: DirectoryProperty
	}

	override fun obtain() = mutableSetOf<String>().also { out ->
		parameters.pluginsDir.asFileTree.visit(object : EmptyFileVisitor() {
			override fun visitFile(visit: FileVisitDetails) {
				val relativePath = visit.relativePath
				val segments = relativePath.segments
				var n = segments.size
				if (n <= 1 || segments[--n] != "$PLUGIN_CLASS.kt") {
					throw InvalidUserDataException("Only `$PLUGIN_CLASS.kt` files are supported.\n- Invalid file: $relativePath")
				}
				out.add(buildString {
					append(segments[0])
					for (i in 1 until n)
						append('.').append(segments[i])
				})
			}
		})
	}
}
