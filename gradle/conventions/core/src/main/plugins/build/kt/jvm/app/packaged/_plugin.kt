package build.kt.jvm.app.packaged

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.application
import build.api.dsl.accessors.distributions
import build.api.dsl.accessors.java
import build.api.dsl.accessors.javaToolchains
import build.api.dsl.accessors.jvmArgs
import build.api.dsl.accessors.kotlinSourceSets
import build.api.platform.Os
import build.kt.jvm.app.DIST_APP_HOME_INSTALL_TASK_NAME
import build.kt.jvm.app.packaged.linux.JPackageSetupDeb
import build.kt.jvm.app.packaged.linux.JPackageSetupRpm
import build.kt.jvm.app.packaged.mac.JPackageSetupDmg
import build.kt.jvm.app.packaged.mac.JPackageSetupPkg
import build.kt.jvm.app.packaged.win.JPackageSetupExe
import build.kt.jvm.app.packaged.win.JPackageSetupMsi
import build.kt.jvm.app.packaged.win.JPackageSetupWindowsBaseTask
import build.kt.jvm.app.packaged.win.WixInstallTask
import com.github.jengelman.gradle.plugins.shadow.ShadowApplicationPlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Sync
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

	val packaged: PackagedSpec = xs().create("packaged")
	val distributionBaseName = distributions.named("main").flatMap { it.distributionBaseName }
	packaged.bundleName.convention(distributionBaseName)
	packaged.appNs.convention(provider { "$group.$name" })

	val jpackageResources = packaged.jpackageResources
	jpackageResources.set(layout.projectDirectory.dir("src/main/jpackage-res"))
	kotlinSourceSets.named("main") {
		val jpackageRes = "jpackageRes".let { this.project.objects.sourceDirectorySet(it, it) }
		jpackageRes.srcDir(jpackageResources)
		jpackageRes.exclude("*")
		resources.source(jpackageRes)
	}

	packaged.jvmArgs.run {
		addAll(application.jvmArgs)
	}

	val tasks = tasks
	tasks.named<Sync>(DIST_APP_HOME_INSTALL_TASK_NAME).let {
		packaged.bundleAdditions.from(it)
	}

	// --

	val packaged_validate = tasks.register<PackagedSpecValidationTask>("checkPackagedSpec") {
		spec = packaged
	}.also {
		tasks.named("check") { dependsOn(it) }
		tasks.runOnIdeSync(it)
	}

	val jdkInstallationFromToolchain = javaToolchains.compilerFor(java.toolchain).map { it.metadata }
	val jdkHomeFromToolchain = jdkInstallationFromToolchain.map { it.installationPath }

	val installShadowDist = tasks.named<Sync>(ShadowApplicationPlugin.SHADOW_INSTALL_TASK_NAME)
	val shadowJar = tasks.named<ShadowJar>(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME)

	val jpackageBuildDir = layout.buildDirectory.dir("jpackage")
	val jpackageDist by tasks.registering(JPackageDist::class) {
		group = DISTRIBUTION_GROUP

		runtimeVersion = jdkInstallationFromToolchain.map { it.languageVersion.asInt() }
		jdkHome = jdkHomeFromToolchain
		spec = packaged
		dependsOn(packaged_validate)

		outputDir = jpackageBuildDir.flatMap { it.dir(packaged.bundleName) }

		appDir = installShadowDist.map { File(it.destinationDir, "lib") }
		mainJar = shadowJar.flatMap { it.archiveFile }.map { it.asFile.name }
	}

	Action<AbstractArchiveTask> {
		group = DISTRIBUTION_GROUP

		from(jpackageDist)
		destinationDirectory = jpackageBuildDir
		archiveBaseName = packaged.bundleName
	}.also { config ->
		tasks.register("jpackageDistZip", Zip::class.java, config)
		tasks.register("jpackageDistTar", Tar::class.java, config)
	}

	Action<JPackageSetupBaseTask> {
		group = DISTRIBUTION_GROUP

		jdkHome = jdkHomeFromToolchain
		spec = packaged
		dependsOn(packaged_validate)

		val outputFileName = packaged.bundleName.map { "$it-setup.$type" }
		outputFile = jpackageBuildDir.flatMap { it.file(outputFileName) }

		appImage = jpackageDist.flatMap { it.outputDir }
	}.also(fun(config) {
		val wixBinZipUrlProp: Property<String> = objects.property()
		projectThis.xs().add(typeOf(), "wixBinZipUrl", wixBinZipUrlProp)

		when (Os.current) {
			Os.WINDOWS -> {
				val installWix by tasks.registering(WixInstallTask::class) {
					wixBinZipUrl = wixBinZipUrlProp

					val buildDir = this.project.layout.buildDirectory
					wixBinZipDestination = buildDir.file("wix/bin.zip")
					wixBinZipPropertiesDestination = buildDir.file("wix/bin.zip.properties")
					wixBinDestination = buildDir.dir("wix/bin")
				}

				Action<JPackageSetupWindowsBaseTask> {
					config.execute(this)
					wixBinaries = installWix.flatMap { it.wixBinDestination }
				}.let(fun(config) {
					tasks.register("jpackageSetupExe", JPackageSetupExe::class.java, config)
					tasks.register("jpackageSetupMsi", JPackageSetupMsi::class.java, config)
				})
			}

			Os.MACOS -> {
				tasks.register("jpackageSetupDmg", JPackageSetupDmg::class.java, config)
				tasks.register("jpackageSetupPkg", JPackageSetupPkg::class.java, config)
			}

			Os.LINUX -> {
				tasks.register("jpackageSetupDeb", JPackageSetupDeb::class.java, config)
				tasks.register("jpackageSetupRpm", JPackageSetupRpm::class.java, config)
			}
		}
	})
})
