package kokoro.internal.collections

import androidx.collection.MutableScatterMap
import kokoro.internal.ASSERTIONS_ENABLED

inline fun <K, V> MutableScatterMap<K, V>.initWithAssert(key: K, value: V, or: () -> String = { "Already set.\n- Key: $key\n- Value: $value" }) {
	if (!ASSERTIONS_ENABLED) {
		set(key, value)
	} else {
		val prev = put(key, value)
		if (prev != null) throw AssertionError(or())
	}
}
