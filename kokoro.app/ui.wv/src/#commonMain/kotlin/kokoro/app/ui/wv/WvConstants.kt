package kokoro.app.ui.wv

internal const val WIDGET_ID_SLOT_BITS = 4
internal const val WIDGET_ID_INC = 1 shl WIDGET_ID_SLOT_BITS

//region Widget Status Flags

internal const val WS_TRACKED = 1 shl 0

internal const val WS_GARBAGE = WS_TRACKED or (1 shl 1)
internal const val WS_GARBAGE_INV = WS_TRACKED or WS_GARBAGE.inv()

internal const val WS_MODEL_UPDATE = WS_TRACKED or (1 shl 2)
internal const val WS_MODIFIER_UPDATE = WS_TRACKED or (1 shl 3)
internal const val WS_UPDATE = WS_MODIFIER_UPDATE or WS_MODEL_UPDATE

//endregion
