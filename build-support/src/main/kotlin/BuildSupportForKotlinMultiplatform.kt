import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

class BuildSupportForKotlinMultiplatform : Plugin<Project> {

	override fun apply(project: Project) {
		val plugins = project.plugins
		plugins.apply("build-support")

		// Make sure kotlin multiplatform plugin is applied first; throws otherwise.
		plugins.getPlugin("org.jetbrains.kotlin.multiplatform")
		doApply(project)
	}

	private fun doApply(project: Project) {
		// The kotlin multiplatform plugin doesn't (yet) do this for us :P
		project.dependencies.extensions.let { exts ->
			// NOTE: Extensions added at configuration time doesn't (yet) generate accessors. Which is why we must do
			// this here :P -- See, https://docs.gradle.org/current/userguide/kotlin_dsl.html#kotdsl:accessor_applicability
			val knownSourceSetNames = setOf(
				"android", "desktop", "jvm"
			)
			for (name in knownSourceSetNames)
				exts.addKnownSourceSetName(name)
		}
	}

	private fun ExtensionContainer.addKnownSourceSetName(name: String) {
		addKnownSourceSetName2("${name}Main")
		addKnownSourceSetName2("${name}Test")
	}

	private fun ExtensionContainer.addKnownSourceSetName2(name: String) {
		"${name}Api".let { add(it, it) }
		"${name}CompileOnly".let { add(it, it) }
		"${name}Implementation".let { add(it, it) }
		"${name}RuntimeOnly".let { add(it, it) }
	}
}
