package conv.internal.support

inline fun <reified T> Any?.unsafeCast() = this as T
