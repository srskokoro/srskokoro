package kokoro.internal.collections

@JsName("Map")
open external class JsMap<K, V> : JsIterable<JsMapEntry<K, V>> {

	val size: Int

	@JsName("has")
	fun containsKey(key: K): Boolean

	@JsName("has")
	operator fun contains(key: K): Boolean

	fun has(key: K): Boolean

	operator fun get(key: K): V?

	fun keys(): JsIterable<K>

	fun values(): JsIterable<V>

	fun entries(): JsIterable<JsMapEntry<K, V>>

	operator fun set(key: K, value: V)

	@JsName("set")
	fun put(key: K, value: V)

	@JsName("delete")
	fun remove(key: K): Boolean

	fun clear()
}

val <K> JsMap<K, *>.keys inline get() = keys()

val <V> JsMap<*, V>.values inline get() = values()

val <K, V> JsMap<K, V>.entries inline get() = entries()

external interface JsMapEntry<@Suppress("unused") out K, @Suppress("unused") out V>

val <K> JsMapEntry<K, *>.key: K
	inline get() = @Suppress("UnsafeCastFromDynamic") asDynamic()[0]

val <V> JsMapEntry<*, V>.value: V
	inline get() = @Suppress("UnsafeCastFromDynamic") asDynamic()[1]

inline operator fun <K> JsMapEntry<K, *>.component1() = key

inline operator fun <V> JsMapEntry<*, V>.component2() = value
