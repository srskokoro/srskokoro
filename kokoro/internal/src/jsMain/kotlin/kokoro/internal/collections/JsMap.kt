package kokoro.internal.collections

@JsName("Map")
open external class JsMap<K, V> {

	val size: Int

	@JsName("has")
	fun containsKey(key: K): Boolean

	@JsName("has")
	operator fun contains(key: K): Boolean

	fun has(key: K): Boolean

	operator fun get(key: K): V?

	operator fun set(key: K, value: V)

	@JsName("delete")
	fun remove(key: K): Boolean

	fun clear()
}
