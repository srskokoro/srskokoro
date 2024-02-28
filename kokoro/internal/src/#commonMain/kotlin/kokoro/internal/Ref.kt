package kokoro.internal

import kotlin.jvm.JvmField

/**
 * A very basic, mutable value holder.
 */
data class Ref<T>(@JvmField var value: T) {
	override fun toString() = value.toString()
}
