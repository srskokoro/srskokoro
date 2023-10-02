package kokoro.app.ui.wv.setup

import XS_wv
import addExtraneousSource
import assets
import conv.internal.setup.*
import conv.util.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.attributes.Usage
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.TEST_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_MAIN_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_TEST_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import wv
import java.util.LinkedList
import java.util.function.Function

private const val ATTRIBUTE_NS = "kokoro.app.ui.wv.setup"

class WvSetupPlugin : Plugin<Project> {

	override fun apply(targetProject: Project) {
		targetProject.pluginManager.apply("conv.kt.mpp")
		targetProject.pluginManager.apply("conv.kt.mpp.assets")

		val tasks = targetProject.tasks

		val objects = targetProject.objects
		val projectSourceSets = targetProject.kotlinSourceSets

		val configurationSetup = ConfigurationSetup(targetProject)

		projectSourceSets.all {
			val sourceSetName = this.name
			configurationSetup.initBucketConfiguration(sourceSetName)

			val wvDisplayName = "$sourceSetName WebView setup"
			val wv = objects.sourceDirectorySet(wvDisplayName, wvDisplayName)

			wv.include("**/*.wv.js")
			wv.include("**/*.wv.spec")
			wv.include("**/*.wv.lst")

			wv.srcDir("src/$sourceSetName/wv")
			addExtraneousSource(XS_wv, wv)

			val wvSetupGenerateTask = tasks.register<WvSetupGenerateTask>("${sourceSetName}WvSetupGenerate") {
				group = LifecycleBasePlugin.BUILD_GROUP

				val project = this.project
				outputDir.set(project.layout.buildDirectory.dir(project.provider { "generated/$name" }))

				sourceDirectories.from(wv.sourceDirectories)
			}

			this.kotlin.run {
				srcDir(wvSetupGenerateTask)
				source(wv) // Simply for IDE support. Unnecessary otherwise.
			}
		}

		targetProject.kotlinMppExt.targets.all(fun(target) {
			if (target.platformType == KotlinPlatformType.common) {
				return // Skip!
			}
			val targetName = target.targetName
			kotlin.run {
				val mainSourceSet = projectSourceSets.findByName("${targetName}Main")
					?: return // We require it. Skip it then.

				val oc = configurationSetup.configurations.create("${targetName}WvSetupOutgoing")
				// It's a consumable configuration (and not resolvable)
				oc.isCanBeResolved = false

				configurationSetup.setUpTerminalConfiguration(oc, target)
				target.project.afterEvaluate {
					for (s in listOf(mainSourceSet).topDownCollect())
						oc.extendsFrom(configurationSetup.initBucketConfiguration(s.name))
				}
				// NOTE: The common source set isn't necessarily included above,
				// particularly when the default source set doesn't depend on
				// the common source set.
				oc.extendsFrom(configurationSetup.initBucketConfiguration(COMMON_MAIN_SOURCE_SET_NAME))

				val wvSetupExportTask = tasks.register<WvSetupExportTask>("${targetName}WvSetupExport") {
					group = LifecycleBasePlugin.BUILD_GROUP

					val project = this.project
					destinationDirectory.set(project.layout.buildDirectory.dir("wvSetupLibs"))
					target.disambiguationClassifier?.let { archiveAppendix.set(it.lowercase()) }

					// NOTE: The common source set isn't necessarily included by
					// the main source set, particularly when the main source
					// set doesn't depend on the common source set.
					val commonSourceSet = project.kotlinSourceSets.getByName(COMMON_MAIN_SOURCE_SET_NAME)
					from(fun() = listOf(commonSourceSet, mainSourceSet).topDownCollect().map { it.wv })
				}

				oc.outgoing.artifact(wvSetupExportTask)
			}
			target.compilations.all(fun(compilation) {
				val compilationName = compilation.compilationName
				val uCompilationName = compilationName.replaceFirstChar { it.uppercaseChar() }
				val domainNamePrefix = "${targetName}${uCompilationName}"

				val ic = configurationSetup.configurations.create("${domainNamePrefix}WvSetupIncoming")
				// It's a resolvable configuration (and not consumable)
				ic.isCanBeConsumed = false

				configurationSetup.setUpTerminalConfiguration(ic, target)
				compilation.project.afterEvaluate {
					for (s in compilation.allKotlinSourceSets)
						ic.extendsFrom(configurationSetup.initBucketConfiguration(s.name))
					for (c in compilation.associateWith) for (s in c.allKotlinSourceSets)
						ic.extendsFrom(configurationSetup.initBucketConfiguration(s.name))
				}
				// NOTE: The common source set isn't necessarily included above,
				// particularly when the default source set doesn't depend on
				// the common source set.
				ic.extendsFrom(configurationSetup.initBucketConfiguration(COMMON_MAIN_SOURCE_SET_NAME))
				if (compilationName == TEST_COMPILATION_NAME) {
					ic.extendsFrom(configurationSetup.initBucketConfiguration(COMMON_TEST_SOURCE_SET_NAME))
				}

				val wvSetupBuildTask = tasks.register<WvSetupBuildTask>("${domainNamePrefix}WvSetupProcessSpec") {
					group = LifecycleBasePlugin.BUILD_GROUP

					val project = this.project
					outputDir.set(project.layout.buildDirectory.dir(project.provider { "generated/$name" }))

					classpath.from(ic, fun() = compilation.associateWith.map { c -> c.allKotlinSourceSets.map { it.wv.sourceDirectories } })
					sourceDirectories.from(fun() = compilation.allKotlinSourceSets.map { it.wv.sourceDirectories })
				}

				val d = compilation.defaultSourceSet
				d.kotlin.srcDir(wvSetupBuildTask.map { it.kotlinOutputDir })
				d.assets.srcDir(wvSetupBuildTask.map { it.assetsOutputDir })
			})
		})
	}

	private class ConfigurationSetup(
		project: Project,
	) : Function<String, Configuration> {
		val configurations: ConfigurationContainer = project.configurations

		private val mapOfSourceSetToBucketConfiguration = HashMap<String, Configuration>()

		fun initBucketConfiguration(sourceSetName: String): Configuration =
			mapOfSourceSetToBucketConfiguration.computeIfAbsent(sourceSetName, this)

		override fun apply(sourceSetName: String): Configuration = configurations.create(
			"${sourceSetName}WvSetup"
		) {
			// It's simply a bucket of dependencies
			isCanBeResolved = false
			isCanBeConsumed = false
		}

		// --

		private val toCopyTargetAttributesAfterEvaluate = LinkedList<Pair<KotlinTarget, Configuration>>()

		init {
			project.afterEvaluate {
				for ((target, co) in toCopyTargetAttributesAfterEvaluate)
					co.attributes.attributesFrom(target.attributes)
			}
		}

		fun setUpTerminalConfiguration(terminal: Configuration, target: KotlinTarget) {
			terminal.isVisible = false // It should only be for internal use

			toCopyTargetAttributesAfterEvaluate.addLast(target to terminal)
			terminal.attributes.apply {
				attribute(KotlinPlatformType.attribute, target.platformType)
				attribute(Usage.USAGE_ATTRIBUTE, target.project.objects.named("$ATTRIBUTE_NS:usage"))
			}
		}
	}
}
