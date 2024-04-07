package build.api.dsl

import build.api.Transformer
import org.gradle.api.provider.Provider

@Suppress("NOTHING_TO_INLINE")
inline fun <R, T> Provider<T>.mapNotNull(transformer: Transformer<out R?, in T>): Provider<R> = map(transformer)
