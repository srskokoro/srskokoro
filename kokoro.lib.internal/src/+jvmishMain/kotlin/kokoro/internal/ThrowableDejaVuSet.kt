package kokoro.internal

import java.util.Collections
import java.util.IdentityHashMap

@JvmInline
value class ThrowableDejaVuSet private constructor(val set: MutableSet<Throwable>) {
	constructor() : this(Collections.newSetFromMap(IdentityHashMap()))
}
