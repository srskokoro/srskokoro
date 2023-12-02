package kokoro.app.ui.engine.window

import kokoro.internal.serialization.NullableNothingSerializer
import korlibs.datastructure.identityHashCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

sealed interface WindowId<A : WindowArgs> {
	val classFqn: String

	val rootId: RootId<A>
	val instanceKey: String?

	/**
	 * See also, “[Plugin-generated serializer | kotlinx.serialization/docs/serializers.md · Kotlin/kotlinx.serialization | GitHub](https://github.com/Kotlin/kotlinx.serialization/blob/v1.6.1/docs/serializers.md#plugin-generated-serializer)”
	 */
	fun SerializersModule.argsSerializer(): KSerializer<A>

	abstract class RootId<A : WindowArgs> @PublishedApi internal constructor() : WindowId<A> {
		final override val rootId: RootId<A> get() = this
		final override val instanceKey: String? get() = null

		final override fun toString() = toString(null)

		internal fun toString(instanceKeyOverride: String?) = buildString {
			append("WindowId<*>(classFqn=")
			append(classFqn)
			if (instanceKeyOverride != null) {
				append(", instanceKey=")
				append(instanceKeyOverride)
			}
			append(")@")
			append(this@RootId.identityHashCode().toUInt().toString(16))
		}
	}
}

private data class InstancedWindowId<A : WindowArgs>(
	override val rootId: WindowId.RootId<A>,
	override val instanceKey: String,
) : WindowId<A> {

	override val classFqn: String get() = rootId.classFqn

	override fun SerializersModule.argsSerializer(): KSerializer<A> {
		return with(rootId) { argsSerializer() }
	}

	override fun toString() = rootId.toString(instanceKey)
}

// --

@Suppress("NOTHING_TO_INLINE")
inline fun WindowId(clazz: KClass<out WindowCore<Nothing?>>): WindowId.RootId<Nothing?> = WindowId(clazz) { NullableNothingSerializer() }

inline fun <reified A : WindowArgs> WindowId(clazz: KClass<out WindowCore<A>>): WindowId.RootId<A> = WindowId(clazz) { serializer<A>() }

inline fun <A : WindowArgs> WindowId(
	clazz: KClass<out WindowCore<A>>,
	crossinline argsSerializer: SerializersModule.() -> KSerializer<A>,
) = WindowId_unsafe(clazz.qualifiedName ?: throw WindowId_classFqn_fail(clazz), argsSerializer)

// --

@PublishedApi
internal inline fun <A : WindowArgs> WindowId_unsafe(
	classFqn: String,
	crossinline argsSerializer: SerializersModule.() -> KSerializer<A>,
): WindowId.RootId<A> = object : WindowId.RootId<A>() {
	override val classFqn: String get() = classFqn
	override fun SerializersModule.argsSerializer() = argsSerializer()
}

@PublishedApi
internal fun WindowId_classFqn_fail(clazz: KClass<out WindowCore<*>>) = UnsupportedOperationException(
	"Unsupported class: $clazz\nMake sure the class is neither local nor anonymous."
)

// --

@Suppress("NOTHING_TO_INLINE")
inline operator fun <A : WindowArgs> WindowId<A>.invoke() = this

@Suppress("NOTHING_TO_INLINE")
@JvmName("invoke_nn")
inline operator fun <A : WindowArgs> WindowId<A>.invoke(instanceKey: String) = of(instanceKey)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <A : WindowArgs> WindowId<A>.invoke(instanceKey: String?) = of(instanceKey)

@JvmName("of_nn")
fun <A : WindowArgs> WindowId<A>.of(instanceKey: String): WindowId<A> {
	return InstancedWindowId(this.rootId, instanceKey)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <A : WindowArgs> WindowId<A>.of(instanceKey: String?): WindowId<A> {
	return if (instanceKey == null) this.rootId else of(instanceKey)
}
