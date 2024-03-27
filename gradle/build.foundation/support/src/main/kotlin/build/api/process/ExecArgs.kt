package build.api.process

import java.util.LinkedList

@Suppress("NOTHING_TO_INLINE")
open class ExecArgs : LinkedList<String>() {

	inline fun args() = this

	inline fun args(arg1: String) = apply {
		add(arg1)
	}

	inline fun args(arg1: String, arg2: String) = apply {
		add(arg1)
		add(arg2)
	}

	inline fun args(arg1: String, arg2: String, arg3: String) = apply {
		add(arg1)
		add(arg2)
		add(arg3)
	}

	inline fun args(arg1: String, arg2: String, arg3: String, arg4: String) = apply {
		add(arg1)
		add(arg2)
		add(arg3)
		add(arg4)
	}

	inline fun args(vararg args: String) = argsFrom(args)

	inline fun args(args: Collection<String>) = argsFrom(args)
	inline fun args(args: Iterable<String>) = argsFrom(args)
	inline fun args(args: Sequence<String>) = argsFrom(args)

	inline fun argsFrom(args: Collection<String>) = apply { addAll(args) }
	inline fun argsFrom(args: Iterable<String>) = apply { addAll(args) }
	inline fun argsFrom(args: Sequence<String>) = apply { addAll(args) }
	inline fun argsFrom(args: Array<out String>) = apply { addAll(args) }

	companion object {

		inline operator fun invoke(
			configure: ExecArgs.() -> Unit = {},
		) = ExecArgs().apply(configure)
	}
}
