import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
	id("build.plugin.root")
}

rootProject.let(fun(rootProject) = rootProject.plugins.withType<YarnPlugin>(fun(_) {
	with(rootProject.extensions.getByName(YarnRootExtension.YARN) as YarnRootExtension) {
		lockFileDirectory = File(rootProject.projectDir, "#kotlin-js-store")
	}
}))
