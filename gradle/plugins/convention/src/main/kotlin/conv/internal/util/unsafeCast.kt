package conv.internal.util

internal inline fun <reified T> Any.unsafeCast() = this as T
