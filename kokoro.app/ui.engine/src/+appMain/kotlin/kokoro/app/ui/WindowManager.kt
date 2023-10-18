package kokoro.app.ui

abstract class WindowManager {
	private val entries = mutableMapOf<String, Entry>()

	abstract fun launch(spec: WindowSpec, args: List<Any?>)

	abstract fun post(spec: WindowSpec, args: List<Any?>)

	protected abstract class Entry(
		val spec: WindowSpec,
		val state: WindowState,
	)

	/**
	 * CONTRACT: Must call [WindowSpec.onNewArgs]`()` with the newly created
	 * [WindowState] before returning.
	 */
	protected abstract fun newWindowEntry(
		specKey: String,
		spec: WindowSpec,
		args: List<Any?>,
	): Entry

	/// --

	protected fun onLaunch(spec: WindowSpec, args: List<Any?>) {
		val specKey = spec.key
		val entry = entries[specKey]
		if (entry != null) {
			entry.spec.onNewArgs(entry.state, args)
		} else {
			entries[specKey] = newWindowEntry(specKey, spec, args)
		}
	}

	protected fun onPost(spec: WindowSpec, args: List<Any?>) {
		val entry = entries[spec.key] ?: return
		entry.spec.onNewArgs(entry.state, args)
	}
}
