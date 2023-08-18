package conv.util

import org.gradle.api.provider.Provider

fun <T> obtain(vararg providers: Provider<T>): List<T> = providers.map { it.get() }

/** @see obtain */
fun <T> obtain(vararg values: T): List<T> = listOf(*values)
