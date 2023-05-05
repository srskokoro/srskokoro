package kokoro.internal

import java.util.*

@JvmInline
value class ThrowableDejaVuSet private constructor(
	private val actual: MutableSet<Throwable>
) : MutableSet<Throwable> by actual {
	constructor() : this(Collections.newSetFromMap(IdentityHashMap()))
}
