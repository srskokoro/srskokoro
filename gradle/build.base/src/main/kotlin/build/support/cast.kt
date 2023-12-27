package build.support

inline fun <reified T> Any?.cast() = this as T

inline fun <reified T> Any?.castSafely() = this as? T
