package build.internal.support

inline fun <reified T> Any?.unsafeCast() = this as T

inline fun <reified T> Any?.unsafeCastOrNull() = this as? T