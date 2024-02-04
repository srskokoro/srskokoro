package build.dependencies

import org.gradle.api.InvalidUserDataException
import org.gradle.kotlin.dsl.*
import java.io.File
import java.util.LinkedList

open class BaseDependencySettings internal constructor() {

	val props = DependencySettings.Props()
	val plugins = LinkedHashMap<PluginId, String>()
	val modules = LinkedHashMap<ModuleId, String>()

	// --

	fun prop(key: String, value: Any) = prop(key, value.toString())

	fun prop(key: String, value: String) {
		if (props.putIfAbsent(key, value) != null)
			throw E_DuplicatePropKey(key)
	}

	/**
	 * @see KotlinId
	 * @see KotlinId.pluginId
	 * @see KotlinId.moduleId
	 */
	@Suppress("NOTHING_TO_INLINE")
	inline fun kotlin(module: String) = KotlinId(module)

	/**
	 * See, [Plugin Marker Artifacts | Using Plugins | Gradle User Manual](https://docs.gradle.org/8.5/userguide/plugins.html#sec:plugin_markers)
	 */
	@Suppress("NOTHING_TO_INLINE")
	inline fun pluginMarker(pluginId: String) = "$pluginId:$pluginId.gradle.plugin"

	@Suppress("NOTHING_TO_INLINE")
	inline fun pluginMarker(kotlinId: KotlinId) = pluginMarker(kotlinId.pluginId())

	/** @see pluginMarker */
	@Suppress("NOTHING_TO_INLINE")
	inline fun String.plugin() = pluginMarker(this)

	/** @see pluginMarker */
	@Suppress("NOTHING_TO_INLINE")
	inline fun KotlinId.plugin() = pluginMarker(this)

	fun plugin(pluginId: KotlinId, version: String) =
		plugin(pluginId.pluginId(), version)

	fun plugin(pluginId: String, version: String) {
		if (plugins.putIfAbsent(PluginId.of(pluginId), version) != null)
			throw E_DuplicatePluginId(pluginId)
	}

	fun module(moduleId: KotlinId, version: String) =
		module(moduleId.moduleId(), version)

	fun module(moduleId: String, version: String) {
		if (modules.putIfAbsent(ModuleId.of(moduleId), version) != null)
			throw E_DuplicateModuleId(moduleId)
	}

	fun module(group: String, name: String, version: String) {
		val moduleId = ModuleId.of(group, name)
		if (modules.putIfAbsent(moduleId, version) != null)
			throw E_DuplicateModuleId(moduleId)
	}

	// --

	private companion object {

		private fun E_DuplicateEntry(kind: String, name: Any, nameKind: String) = InvalidUserDataException(
			"Cannot add $kind \"$name\" as a $kind with that $nameKind already exists."
		)

		private fun E_DuplicatePropKey(key: String) = E_DuplicateEntry("prop", key, "key")

		private fun E_DuplicatePluginId(pluginId: Any) = E_DuplicateEntry("plugin", pluginId, "name")

		private fun E_DuplicateModuleId(moduleId: Any) = E_DuplicateEntry("module", moduleId, "name")
	}

	/**
	 * See the NOTE in the loading logic to understand why this method is named
	 * like this.
	 *
	 * @see DependencySettings.setUpForUsageInProjects
	 */
	internal fun prioritizeForLoad(rootProject: File) {
		includedBuildsDeque.addLast(rootProject.canonicalPath)
	}

	internal val includedBuildsDeque = LinkedList<String>()
}
