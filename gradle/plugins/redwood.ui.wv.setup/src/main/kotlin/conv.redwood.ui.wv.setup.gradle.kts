import conv.internal.setup.*
import conv.redwood.ui.wv.setup.WvSetupGenerateTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_MAIN_SOURCE_SET_NAME

plugins {
	id("app.cash.redwood.generator.widget")
	id("conv.kt.mpp.assets")
}

run<Unit> {
	val generate = tasks.register("redwoodGenerateWvSetupConv", WvSetupGenerateTask::class.java) {
		group = LifecycleBasePlugin.BUILD_GROUP

		val project = this.project
		classpath.from(project.configurations.redwoodSchema)
		schemaPackage.set(project.redwoodSchema.type.map { it.substringBeforeLast('.', "") })

		outputDir.set(project.layout.buildDirectory.dir("generated/redwoodWvSetupConv"))

		isDebugBuild.set(project.isDebug)
	}
	afterEvaluate {
		if (!pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
			throw AssertionError("Redwood should've already thrown for us")
		}

		val kotlin: KotlinMultiplatformExtension by extensions
		val commonMain = getSourceSets(kotlin).getByName(COMMON_MAIN_SOURCE_SET_NAME)

		commonMain.kotlin.srcDir(generate.map { it.kotlinOutput })
		commonMain.assets?.srcDir(generate.map { it.assetsOutput })
	}
}
