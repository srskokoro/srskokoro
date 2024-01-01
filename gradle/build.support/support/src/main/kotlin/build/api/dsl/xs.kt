package build.api.dsl

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.*

@Suppress("NOTHING_TO_INLINE")
inline fun Any.xs() = (this as ExtensionAware).xs()

@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionAware.xs(): ExtensionContainer = extensions

@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionContainer.xs() = this

@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionsDelegate<*>.xs() = extensions


inline fun <reified O : Any> O.x() = ExtensionsDelegate<O>(xs())

inline fun <reified T> Any.x(name: String): T = xs().x(name)

inline fun <reified T> Any.x(name: String, noinline configure: T.() -> Unit): Unit = xs().x(name, configure)

inline fun <reified T> Any.x(noinline configure: T.() -> Unit): Unit = xs().x(configure)


@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionContainer.x() = ExtensionsDelegate<Any>(this)

inline fun <reified T> ExtensionContainer.x(name: String): T = getOrThrow(name)

inline fun <reified T> ExtensionContainer.x(name: String, noinline configure: T.() -> Unit): Unit = configure(name, configure)

inline fun <reified T> ExtensionContainer.x(noinline configure: T.() -> Unit): Unit = configure(configure)


inline fun <reified O> ExtensionsDelegate<O>.x() = this

inline fun <reified T> ExtensionsDelegate<*>.x(name: String): T = xs().x(name)

inline fun <reified T> ExtensionsDelegate<*>.x(name: String, noinline configure: T.() -> Unit): Unit = xs().x(name, configure)

inline fun <reified T> ExtensionsDelegate<*>.x(noinline configure: T.() -> Unit): Unit = xs().x(configure)
