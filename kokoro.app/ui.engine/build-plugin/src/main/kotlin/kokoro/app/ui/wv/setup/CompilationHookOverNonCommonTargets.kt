package kokoro.app.ui.wv.setup

import assets
import conv.util.*
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileTree
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.TEST_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_MAIN_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_TEST_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.utils.ObservableSet
import java.util.LinkedList
import java.util.concurrent.Callable
import java.util.function.Function

private const val ATTRIBUTE_NS = "kokoro.app.ui.wv.setup"

internal class CompilationHookOverNonCommonTargets(
	private val configurations: ConfigurationContainer,
	private val projectSourceSets: NamedDomainObjectContainer<KotlinSourceSet>,
	private val wvSetupSourceSets: NamedDomainObjectContainer<WvSetupSourceSet>,
) : Action<KotlinCompilation<*>>, Function<String, Configuration> {

	override fun execute(compilation: KotlinCompilation<*>) {
		val allSourceSets = compilation.allKotlinSourceSets

		val compilationName = compilation.compilationName
		val target = compilation.target
		val project = target.project

		// --
		// Set up for outgoing

		if (compilationName == MAIN_COMPILATION_NAME) {
			val oc = configurations.create("${target.targetName}WvSetupOutgoing")
			// It's a consumable configuration
			oc.isCanBeResolved = false

			setUpTerminalConfiguration(oc, target)
			attachIntermediateConfigurations(oc, allSourceSets)

			val wvSetupExportTask = project.tasks.register<WvSetupExportTask>("${target.targetName}WvSetupExport") {
				group = LifecycleBasePlugin.BUILD_GROUP

				@Suppress("NAME_SHADOWING") val project = this.project
				destinationDirectory.set(project.layout.buildDirectory.dir("wvSetupLibs"))
				target.disambiguationClassifier?.let { archiveAppendix.set(it.lowercase()) }

				from(Callable { allSourceSets.map { it.kotlin } })
			}

			oc.outgoing.artifact(wvSetupExportTask)
		}

		// --
		// Set up for incoming

		val uCompilationName = compilationName.replaceFirstChar { it.uppercaseChar() }
		val ic = configurations.create("${target.targetName}${uCompilationName}WvSetupIncoming")
		// It's a resolvable configuration
		ic.isCanBeConsumed = false

		setUpTerminalConfiguration(ic, target)
		attachIntermediateConfigurations(ic, allSourceSets)

		val inputSources = project.objects.fileCollection()

		// NOTE: Necessary since we'll wire in the configuration's resolved
		// files directly, which doesn't auto-hook task dependencies.
		inputSources.builtBy(ic)
		inputSources.from(Callable(fun() = ArrayList<FileTree>().also { ax ->
			// Wire in the configuration's resolved files directly.
			for (f in ic) {
				val ft = if (f.isFile) project.zipTree(f)
				else if (f.isDirectory) project.fileTree(f)
				else continue
				ax.add(ft)
			}
			// Wire the compilation's `kotlin` source directory sets.
			for (s in allSourceSets) {
				// NOTE: The following is expected to also auto-hook the
				// source directory set's task dependency.
				ax.add(s.kotlin)
			}
		}))

		val wvSetupSourceSets = wvSetupSourceSets
		val wvSetupGenerateJsTask = project.tasks.register<WvSetupGenerateJsTask>("${target.targetName}${uCompilationName}WvSetupGenerateJs") {
			group = LifecycleBasePlugin.BUILD_GROUP

			@Suppress("NAME_SHADOWING") val project = this.project
			outputDir.set(project.layout.buildDirectory.dir(project.provider { "generated/$name" }))

			// NOTE: The following resolves only requests under the current
			// compilation without including those from any of its associate
			// compilations, since the associate compilations are expected to
			// have their own task similar to ours and should automatically
			// execute if their source sets somehow get wired to the current
			// compilation.
			val requests = Iterable {
				allSourceSets.flatMap {
					wvSetupSourceSets.getByName(it.name)
						.generateJsRequests
				}.iterator()
			}

			from(requests = requests, inputSources.asFileTree)
		}

		project.afterEvaluate {
			compilation.defaultSourceSet.assets?.srcDir(wvSetupGenerateJsTask)

			compilation.associateWith.forEach(fun(associate) {
				@Suppress("NAME_SHADOWING") val allSourceSets = associate.allKotlinSourceSets
				inputSources.from(Callable { allSourceSets.map { it.kotlin } })
				(allSourceSets as? ObservableSet)?.forAll { s ->
					ic.extendsFrom(initBucketConfiguration(s.name))
				}
			})
		}

		if (compilationName == TEST_COMPILATION_NAME) {
			// NOTE: The common source set isn't necessarily included in the
			// compilation's "all source sets" set: particularly, when the
			// default source set doesn't depend on the common source set.
			inputSources.from(projectSourceSets.getByName(COMMON_TEST_SOURCE_SET_NAME).kotlin)
			ic.extendsFrom(initBucketConfiguration(COMMON_TEST_SOURCE_SET_NAME))
		}
	}

	// --

	private val toCopyTargetAttributesAfterEvaluate = LinkedList<Pair<KotlinTarget, Configuration>>()

	private fun setUpTerminalConfiguration(terminal: Configuration, target: KotlinTarget) {
		terminal.isVisible = false // It should only be for internal use

		// --
		// Set up attributes

		toCopyTargetAttributesAfterEvaluate.addLast(target to terminal)
		terminal.attributes.apply {
			attribute(KotlinPlatformType.attribute, target.platformType)
			attribute(Usage.USAGE_ATTRIBUTE, target.project.objects.named("$ATTRIBUTE_NS:usage"))
		}
	}

	private fun attachIntermediateConfigurations(terminal: Configuration, allSourceSetsOfCompilation: Set<KotlinSourceSet>) {
		val allSourceSets = allSourceSetsOfCompilation as? ObservableSet
			?: return // We require it. Skip it then.

		allSourceSets.forAll { s ->
			terminal.extendsFrom(initBucketConfiguration(s.name))
		}
		// NOTE: The common source set isn't necessarily included in the
		// compilation's "all source sets" set: particularly, when the default
		// source set doesn't depend on the common source set.
		terminal.extendsFrom(initBucketConfiguration(COMMON_MAIN_SOURCE_SET_NAME))
	}

	fun doCopyTargetAttributesAfterEvaluate() {
		for ((target, co) in toCopyTargetAttributesAfterEvaluate)
			co.attributes.attributesFrom(target.attributes)
	}

	// --

	private val mapOfSourceSetToBucketConfiguration = HashMap<String, Configuration>()

	private fun initBucketConfiguration(sourceSetName: String): Configuration =
		mapOfSourceSetToBucketConfiguration.computeIfAbsent(sourceSetName, this)

	override fun apply(sourceSetName: String): Configuration = configurations.create(
		"${sourceSetName}WvSetup"
	) {
		// It's simply a bucket of dependencies
		isCanBeResolved = false
		isCanBeConsumed = false
	}
}
