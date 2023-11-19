package conv.deps

import org.gradle.api.IllegalDependencyNotation
import org.gradle.api.InvalidUserDataException
import org.gradle.api.UnknownDomainObjectException
import java.io.File

// --

internal fun failOnDuplicatePluginId(pluginId: Any): Nothing = throw InvalidUserDataException(
	"""
	Cannot add plugin "$pluginId" as a plugin with that name already exists.
	""".trimIndent()
)

internal fun failOnDuplicateModuleId(moduleId: Any): Nothing = throw InvalidUserDataException(
	"""
	Cannot add module "$moduleId" as a module with that name already exists.
	""".trimIndent()
)

// --

internal fun failOnPluginId(namespace: String, name: String): Nothing = failOnPluginId("$namespace.$name")
internal fun failOnPluginId(pluginId: String): Nothing = throw IllegalDependencyNotation(
	"""
	Supplied plugin identifier "$pluginId" is invalid.
	Example identifiers: "org.jetbrains.kotlin.jvm", "com.android.$ANY_NAME"
	""".trimIndent()
)

internal fun failOnModuleId(group: String, name: String): Nothing = failOnModuleId("$group:$name")
internal fun failOnModuleId(moduleId: String): Nothing = throw IllegalDependencyNotation(
	"""
	Supplied module identifier "$moduleId" is invalid.
	Example identifiers: "org.gradle:gradle-core", "org.mockito:$ANY_NAME"
	""".trimIndent()
)

internal fun failOnVersion(version: String): Nothing = throw IllegalDependencyNotation(
	"""
	Supplied version string "$version" is invalid.
	""".trimIndent()
)

// --

internal fun failOnArgToPluginId(arg: Any): Nothing = failOnArgToTypeX<PluginId>(arg)
internal fun failOnArgToModuleId(arg: Any): Nothing = failOnArgToTypeX<ModuleId>(arg)
internal fun failOnArgToVersion(arg: Any): Nothing = failOnArgToTypeX<Version>(arg)

internal fun failOnArgToFile(arg: Any?): Nothing = failOnArgToTypeX<File>(arg)

private inline fun <reified X> failOnArgToTypeX(arg: Any?): Nothing = throw IllegalArgumentException(
	"""
	Cannot convert to ${X::class.java.name}: $arg
	""".trimIndent()
)

// --

@PublishedApi
internal fun failOnJvmSetupVendor(jvmVendorSpec: String): Nothing = throw UnknownDomainObjectException(
	"""
	Unknown JVM vendor spec: $jvmVendorSpec
	""".trimIndent()
)

@PublishedApi
internal fun failOnJvmSetupImplementation(jvmImplementation: String): Nothing = throw UnknownDomainObjectException(
	"""
	Unknown JVM implementation: $jvmImplementation
	""".trimIndent()
)
