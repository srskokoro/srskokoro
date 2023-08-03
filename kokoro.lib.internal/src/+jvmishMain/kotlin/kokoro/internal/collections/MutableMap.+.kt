package kokoro.internal.collections

actual fun interface MapComputeFunction<in K, out V> : java.util.function.Function<@UnsafeVariance K, @UnsafeVariance V>

@Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER")
actual inline fun <K, V> MutableMap<K, V>.computeIfAbsent(key: K, mappingFunction: MapComputeFunction<K, V>): V {
	return computeIfAbsent(key, mappingFunction)
}

actual inline fun <K, V> MutableMap<K, V>.computeIfAbsent(key: K, crossinline mappingFunction: (K) -> V): V {
	return computeIfAbsent(key) { mappingFunction(it) }
}
