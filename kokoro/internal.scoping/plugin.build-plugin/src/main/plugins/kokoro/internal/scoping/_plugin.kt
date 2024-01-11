package kokoro.internal.scoping

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class _plugin : KotlinCompilerPluginSupportPlugin {

	override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

	override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
		return kotlinCompilation.target.project.provider {
			listOf() // No options.
		}
	}

	override fun getCompilerPluginId() = COMPILER_PLUGIN_ID

	override fun getPluginArtifact() = SubpluginArtifact(
		COMPILER_ARTIFACT_GROUP,
		COMPILER_ARTIFACT_NAME,
	)
}
