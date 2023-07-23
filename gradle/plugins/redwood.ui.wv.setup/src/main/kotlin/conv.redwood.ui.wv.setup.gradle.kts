import conv.internal.setup.*
import conv.redwood.ui.wv.setup.WvSetupGenerateTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_MAIN_SOURCE_SET_NAME

plugins {
	id("conv.kt.mpp.assets")
	id("app.cash.redwood.generator.widget")
}

run<Unit> {
	val generate = tasks.register("redwoodGenerateWvSetupConv", WvSetupGenerateTask::class.java) {
		group = LifecycleBasePlugin.BUILD_GROUP

		val project = this.project
		classpath.from(project.configurations.redwoodSchema)
		schemaPackage.set(project.redwoodSchema.type.map { it.substringBeforeLast('.', "") })

		outputDir.set(project.layout.buildDirectory.dir("generated/redwoodWvSetupConv"))
	}
	afterEvaluate {
		if (!pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) return@afterEvaluate

		val kotlin: KotlinMultiplatformExtension by extensions
		val commonMain = getSourceSets(kotlin).getByName(COMMON_MAIN_SOURCE_SET_NAME)

		commonMain.kotlin.srcDir(generate.map { it.kotlinOutput })
		commonMain.assets?.srcDir(generate.map { it.assetsOutput })
	}
}
