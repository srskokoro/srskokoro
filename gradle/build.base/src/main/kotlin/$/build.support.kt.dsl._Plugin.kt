@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.plugin.devel.PluginDeclaration

inline fun NamedDomainObjectContainer<PluginDeclaration>.projectPlugin(
	id: String, crossinline configure: PluginDeclaration.() -> Unit = {},
) {
	register(id) {
		this.id = id
		this.implementationClass = "$id._projectPlugin"
		configure()
	}
}

inline fun NamedDomainObjectContainer<PluginDeclaration>.settingsPlugin(
	id: String, crossinline configure: PluginDeclaration.() -> Unit = {},
) {
	register(id) {
		this.id = id
		this.implementationClass = "$id._settingsPlugin"
		configure()
	}
}
