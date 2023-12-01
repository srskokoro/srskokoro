package kokoro.internal.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.builtins.nullable

@OptIn(ExperimentalSerializationApi::class)
private val NullableNothingSerializer = NothingSerializer().nullable

fun NullableNothingSerializer(): KSerializer<Nothing?> = NullableNothingSerializer
