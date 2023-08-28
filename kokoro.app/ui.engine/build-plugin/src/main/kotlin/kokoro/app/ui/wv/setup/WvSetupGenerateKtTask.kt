package kokoro.app.ui.wv.setup

import kokoro.app.ui.wv.setup.WvSetupGenerateJsTask.Companion.WV_JS
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.mapProperty
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import javax.inject.Inject

@CacheableTask
abstract class WvSetupGenerateKtTask @Inject constructor(
	objects: ObjectFactory,
	private val providers: ProviderFactory,
) : DefaultTask() {

	@get:OutputDirectory
	val outputDir: DirectoryProperty = objects.directoryProperty()

	@get:Input
	val sources: MapProperty<String, FileTree> = objects.mapProperty()

	init {
		inputs.files(sources.map { it.values })
			.withPathSensitivity(PathSensitivity.RELATIVE)
			.ignoreEmptyDirectories()
			.skipWhenEmpty()
	}

	fun from(srcSets: Iterable<KotlinSourceSet>) {
		sources.putAll(providers.provider(fun() = LinkedHashMap<String, FileTree>().also { map ->
			for (s in srcSets) map[s.name] = matchWvSetupSourcesForKtGeneration(s.kotlin)
		}))
	}

	@TaskAction
	fun generate() {
		// TODO
	}
}

private fun matchWvSetupSourcesForKtGeneration(ft: FileTree) = ft.matching {
	include("**/*.templ.$WV_JS")
	include("**/*.const.$WV_JS")
}
