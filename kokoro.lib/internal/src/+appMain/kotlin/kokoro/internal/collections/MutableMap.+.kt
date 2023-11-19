package kokoro.internal.collections

expect fun interface MapComputeFunction<in K, out V> {
	fun apply(key: K): V
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
expect fun <K, V> MutableMap<K, V>.computeIfAbsent(key: K, mappingFunction: MapComputeFunction<K, V>): V

expect inline fun <K, V> MutableMap<K, V>.computeIfAbsent(key: K, crossinline mappingFunction: (K) -> V): V
