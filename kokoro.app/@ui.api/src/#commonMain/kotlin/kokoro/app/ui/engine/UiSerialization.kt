package kokoro.app.ui.engine

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.JvmField

object UiSerialization {

	@JvmField val module: SerializersModule = EmptySerializersModule() // TODO!
}
