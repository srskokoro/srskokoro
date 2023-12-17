package build.api.dsl

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

@Suppress("NOTHING_TO_INLINE")
inline fun Any.xs() = (this as ExtensionAware).xs()

@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionAware.xs() = extensions

@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionContainer.xs() = this

@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionContainerDelegate.xs() = extensions


@Suppress("NOTHING_TO_INLINE")
inline fun Any.x() = xs().x()

inline fun <reified T> Any.x(name: String): T = xs().x(name)

inline fun <reified T> Any.x(name: String, noinline configure: T.() -> Unit): Unit = xs().x(name, configure)


@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionContainer.x() = ExtensionContainerDelegate(this)

inline fun <reified T> ExtensionContainer.x(name: String): T = getByName(name) as T

inline fun <reified T> ExtensionContainer.x(name: String, noinline configure: T.() -> Unit): Unit = configure(name, configure)
