package kokoro.app.ui.wv.setup

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.RelativePath
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.mapProperty
import javax.inject.Inject

@CacheableTask
abstract class WvSetupGenerateJsTask @Inject constructor(
	objects: ObjectFactory,
	private val providers: ProviderFactory,
) : DefaultTask() {

	companion object {
		/**
		 * The file extension (without the leading dot) expected to be used by
		 * the input JS files (the input sources).
		 */
		const val WV_JS = "wv.js"
	}

	@get:OutputDirectory
	val outputDir: DirectoryProperty = objects.directoryProperty()

	@get:Input
	// K: Output JS (relative to the output directory) without file extension
	// V: Input sources (assumed as already filtered)
	val requests: MapProperty<RelativePath, FileTree> = objects.mapProperty()

	init {
		inputs.files(requests.map { it.values })
			.withPathSensitivity(PathSensitivity.RELATIVE)
			.ignoreEmptyDirectories()
			.skipWhenEmpty()
	}

	fun from(requests: Iterable<WvSetupGenerateJsRequest>, sources: FileTree) {
		this.requests.putAll(providers.provider(fun() = LinkedHashMap<RelativePath, FileTree>().also { map ->
			for (r in requests) map[r.baseJsPathNameAsRelativePath] = matchWvSetupSourcesForJsGeneration(sources, r)
		}))
	}

	@TaskAction
	fun generate() {
		// TODO
	}
}

private fun matchWvSetupSourcesForJsGeneration(ft: FileTree, request: WvSetupGenerateJsRequest) = run {
	val includePatterns = request.inputPackagesAsIncludePatterns
	ft.matching { include(includePatterns) }
}
