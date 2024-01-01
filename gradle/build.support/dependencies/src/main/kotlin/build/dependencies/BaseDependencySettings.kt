package build.dependencies

import java.io.File
import java.util.LinkedList

open class BaseDependencySettings internal constructor() {

	val props = DependencySettings.Props()
	val plugins = LinkedHashMap<PluginId, String>()
	val modules = LinkedHashMap<ModuleId, String>()

	internal val includedBuildsDeque = LinkedList<String>()

	/**
	 * See the NOTE in the loading logic to understand why this method is named
	 * like this.
	 *
	 * @see DependencySettings.setUpForUsageInProjects
	 */
	internal fun prioritizeForLoad(rootProject: File) {
		includedBuildsDeque.addLast(rootProject.canonicalPath)
	}
}
