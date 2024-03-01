package kokoro.internal.collections

import androidx.collection.MutableScatterMap

inline fun <K, V : Any> MutableScatterMap<K, V>.computeIfAbsent(key: K, computeBlock: (key: K) -> V): V {
	return compute(key) { _, prev ->
		if (prev != null) return@computeIfAbsent prev
		computeBlock.invoke(key)
	}
}
