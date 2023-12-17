package build.api.dsl

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

@Suppress("NOTHING_TO_INLINE")
inline fun Any.x() = (this as ExtensionAware).extensions.x()

inline fun <reified T> Any.x(name: String): T = (this as ExtensionAware).extensions.x(name)

inline fun <reified T> Any.x(name: String, noinline configure: T.() -> Unit): Unit = (this as ExtensionAware).extensions.x(name, configure)


@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionContainer.x() = ExtensionContainerScope(this)

inline fun <reified T> ExtensionContainer.x(name: String): T = getByName(name) as T

inline fun <reified T> ExtensionContainer.x(name: String, noinline configure: T.() -> Unit): Unit = configure(name, configure)
