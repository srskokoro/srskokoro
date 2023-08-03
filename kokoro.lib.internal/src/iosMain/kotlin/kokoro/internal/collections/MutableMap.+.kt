package kokoro.internal.collections

actual fun interface MapComputeFunction<in K, out V> {
	actual fun apply(key: K): V
}

actual inline fun <K, V> MutableMap<K, V>.computeIfAbsent(key: K, mappingFunction: MapComputeFunction<K, V>): V {
	return computeIfAbsent(key, mappingFunction::apply)
}

actual inline fun <K, V> MutableMap<K, V>.computeIfAbsent(key: K, mappingFunction: (K) -> V): V {
	val value = get(key)
	if (value == null) {
		// NOTE: Unlike `getOrPut()`, the following ignores the result of the
		// mapping function, if the result is null.
		val newValue = mappingFunction(key)
		if (newValue != null)
			put(key, newValue)
		return newValue
	}
	return value
}
