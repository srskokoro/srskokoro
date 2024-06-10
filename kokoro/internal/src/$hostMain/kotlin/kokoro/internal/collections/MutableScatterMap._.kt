package kokoro.internal.collections

import androidx.collection.MutableScatterMap

inline fun <K, V : Any> MutableScatterMap<K, V>.computeIfAbsent(
	key: K,
	onExisting: (key: K, value: V) -> Unit = { _, _ -> },
	computeBlock: (key: K) -> V,
): V = compute(key) { _, prev ->
	if (prev != null) {
		onExisting(key, prev)
		return@computeIfAbsent prev
	}
	computeBlock.invoke(key)
}

fun <K, V> MutableScatterMap<K, V>.putIfAbsent(key: K, value: V): V? {
	compute(key) { _, prev ->
		if (prev == null) value
		else return@putIfAbsent prev
	}
	return null
}
