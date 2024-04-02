package kokoro.app.ui.engine

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.JvmField

object WvSerialization {

	@JvmField val module: SerializersModule = EmptySerializersModule() // TODO!
}
