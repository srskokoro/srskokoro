package build.kt.jvm.app.packaged

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.java
import build.api.dsl.accessors.javaToolchains
import build.api.dsl.accessors.kotlinSourceSets
import com.github.jengelman.gradle.plugins.shadow.ShadowApplicationPlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.*
import java.io.File

private const val DISTRIBUTION_GROUP = "distribution"

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm.app._plugin>()
	}

	val packaged: PackagedSpec = extensions.create("packaged")
	packaged.bundleName.convention(provider { name })
	packaged.appNs.convention(provider { "$group.$name" })

	val jpackageResources = packaged.jpackageResources
	jpackageResources.set(layout.projectDirectory.dir("src/main/jpackage-res"))
	kotlinSourceSets.named("main") {
		val jpackageRes = "jpackageRes".let { this.project.objects.sourceDirectorySet(it, it) }
		jpackageRes.srcDir(jpackageResources)
		jpackageRes.exclude("*")
		resources.source(jpackageRes)
	}

	val jdkHomeFromToolchain = javaToolchains
		.compilerFor(java.toolchain)
		.map { it.metadata.installationPath }

	val tasks = tasks
	val packaged_validate = tasks.register<PackagedSpecValidationTask>("checkPackagedSpec") {
		spec = packaged
	}.also {
		tasks.named("check") { dependsOn(it) }
		tasks.runOnIdeSync(it)
	}

	val installShadowDist = tasks.named<Sync>(ShadowApplicationPlugin.SHADOW_INSTALL_TASK_NAME)
	val shadowJar = tasks.named<ShadowJar>(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME)

	val jpackageDist by tasks.registering(JPackageDist::class) {
		group = DISTRIBUTION_GROUP

		jdkHome = jdkHomeFromToolchain
		spec = packaged
		dependsOn(packaged_validate)

		outputDir = this.project.layout.buildDirectory
			.dir(packaged.bundleName.map { "jpackage/$it" })

		appDir = installShadowDist.map { File(it.destinationDir, "lib") }
		mainJar = shadowJar.flatMap { it.archiveFile }.map { it.asFile.name }
	}
	tasks.register<Zip>("jpackageDistZip") { fromJPackageDist(jpackageDist) }
	tasks.register<Tar>("jpackageDistTar") { fromJPackageDist(jpackageDist) }
})

private fun AbstractArchiveTask.fromJPackageDist(jpackageDist: TaskProvider<JPackageDist>) {
	group = DISTRIBUTION_GROUP

	from(jpackageDist)
	val jpackageDistOutput = jpackageDist.flatMap { it.outputDir }

	destinationDirectory = jpackageDistOutput.map { it.asFile.parentFile }
	archiveBaseName = jpackageDistOutput.map { it.asFile.name }
}
