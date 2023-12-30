package build.support

/** @see castSafely */
inline fun <reified T> Any?.cast() = this as T

/** @see cast */
inline fun <reified T> Any?.castSafely() = this as? T
