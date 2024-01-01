package build.api.dsl

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

@Suppress("NOTHING_TO_INLINE")
inline fun Any.xs() = (this as ExtensionAware).xs()

@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionAware.xs(): ExtensionContainer = extensions

@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionContainer.xs() = this

@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionsDelegate<*>.xs() = extensions


inline fun <reified O : Any> O.x() = ExtensionsDelegate<O>(xs())

inline fun <reified R> Any.x(name: String): R = xs().x(name)

inline fun <reified R> Any.x(name: String, noinline configure: R.() -> Unit): Unit = xs().x(name, configure)


@Suppress("NOTHING_TO_INLINE")
inline fun ExtensionContainer.x() = ExtensionsDelegate<Any>(this)

inline fun <reified R> ExtensionContainer.x(name: String): R = getOrThrow(name)

inline fun <reified R> ExtensionContainer.x(name: String, noinline configure: R.() -> Unit): Unit = configure(name, configure)


inline fun <reified O> ExtensionsDelegate<O>.x() = this

inline fun <reified R> ExtensionsDelegate<*>.x(name: String): R = xs().x(name)

inline fun <reified R> ExtensionsDelegate<*>.x(name: String, noinline configure: R.() -> Unit): Unit = xs().x(name, configure)
