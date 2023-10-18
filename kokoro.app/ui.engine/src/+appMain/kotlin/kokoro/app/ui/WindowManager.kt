package kokoro.app.ui

abstract class WindowManager {
	private val entries = mutableMapOf<String, Entry>()

	abstract fun launch(spec: WindowSpec, args: List<Any?>)

	abstract fun post(spec: WindowSpec, args: List<Any?>)

	protected abstract class Entry(
		val spec: WindowSpec,
		val state: WindowState,
	)

	protected abstract fun newWindowEntry(
		specKey: String,
		spec: WindowSpec,
		state: WindowState,
	): Entry

	/// --

	protected fun onLaunch(spec: WindowSpec, args: List<Any?>) {
		val newSpec = spec
		val specKey = newSpec.key
		val oldEntry = entries[specKey]

		@Suppress("NAME_SHADOWING")
		val spec: WindowSpec
		val state: WindowState
		if (oldEntry == null) {
			spec = newSpec; state = WindowState()
			spec.onNewArgs(state, args)
		} else {
			spec = oldEntry.spec; state = oldEntry.state
			spec.onNewArgs(state, args)
			return // Skip code below
		}

		val entry = newWindowEntry(specKey, spec, state)
		entries[specKey] = entry
	}

	protected fun onPost(spec: WindowSpec, args: List<Any?>) {
		val entry = entries[spec.key] ?: return
		entry.spec.onNewArgs(entry.state, args)
	}
}
