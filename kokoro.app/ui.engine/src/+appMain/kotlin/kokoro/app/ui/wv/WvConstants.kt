package kokoro.app.ui.wv

internal const val WIDGET_ID_SLOT_BITS = 4
internal const val WIDGET_ID_INC = 1 shl WIDGET_ID_SLOT_BITS

//#region Widget Status Flags

internal const val WS_TRACKED = 1 shl 0

internal const val WS_GARBAGE = 1 shl 1
internal const val WS_GARBAGE_INV = WS_GARBAGE.inv()

internal const val WS_MODEL_UPDATE = 1 shl 2
internal const val WS_MODIFIER_UPDATE = 1 shl 3
internal const val WS_UPDATE = WS_MODIFIER_UPDATE or WS_MODEL_UPDATE

internal const val WS_TRACKED_IN_LAYOUT_STACK = 1 shl 4

internal const val WS_REQUESTED_LAYOUT = 1 shl 5
internal const val WS_TREE_REQUESTED_LAYOUT = WS_REQUESTED_LAYOUT or WS_TRACKED_IN_LAYOUT_STACK

internal const val WS_EXT_SHL = 16
internal const val WS_CORE_MASK = (1 shl WS_EXT_SHL) - 1

//#endregion
