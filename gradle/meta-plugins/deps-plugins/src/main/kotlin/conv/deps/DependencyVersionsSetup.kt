package conv.deps

import dependencyVersions
import ensureDependencyVersions
import org.gradle.api.Action
import org.gradle.api.initialization.ConfigurableIncludedBuild
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware

abstract class DependencyVersionsSetup internal constructor(val settings: Settings) : ExtensionAware {

	private var willUseInProjects = false
	fun useInProjects(extensionName: String = "deps") {
		val spec = settings.ensureDependencyVersions()
		spec.extensionName = extensionName

		if (willUseInProjects) return
		willUseInProjects = true

		spec.setUpForUseInProjects()
	}

	private var willExport = false
	fun export() {
		val spec = settings.ensureDependencyVersions()

		if (willExport) return
		willExport = true

		spec.setUpForExport()
	}

	fun includeBuild(rootProject: Any) {
		// May throw. Thus, we can't be `inline` (or Gradle will report incorrect line numbers :P)
		val spec = settings.dependencyVersions
		spec.includeBuild(rootProject)
	}

	fun includeBuild(rootProject: Any, configuration: Action<ConfigurableIncludedBuild>) {
		// May throw. Thus, we can't be `inline` (or Gradle will report incorrect line numbers :P)
		val spec = settings.dependencyVersions
		spec.includeBuild(rootProject, configuration)
	}
}
