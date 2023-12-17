package build.plugins

import build.api.addExtraneousSourceTo
import build.api.dsl.model.gradlePlugin
import build.api.dsl.model.kotlinJvm
import build.api.dsl.model.kotlinSourceSets
import build.plugins.PluginsAutoRegistrant.Companion.PLUGIN_CLASS
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.EmptyFileVisitor
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * @see PluginsAutoRegistrant
 */
internal fun Project.installPluginsAutoRegistrant() {
	kotlinJvm.kotlinSourceSets.named("main", fun(main) = main.project.run {
		val pluginsDir = file("src/${main.name}/plugins")
		objects.addExtraneousSourceTo(main, "plugins").run {
			srcDir(pluginsDir)
			include("**/$PLUGIN_CLASS.kt")
			main.kotlin.source(this)
		}
		val gradlePlugins = gradlePlugin.plugins
		providers.of(PluginsAutoRegistrant::class.java) {
			parameters.pluginsDir.set(pluginsDir)
		}.get().forEach {
			gradlePlugins.create(it) {
				id = it
				implementationClass = "$it.$PLUGIN_CLASS"
			}
		}
	})
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
