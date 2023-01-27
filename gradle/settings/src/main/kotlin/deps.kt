import java.lang.invoke.VarHandle
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("ClassName")
object deps {
	val plugins: Map<String, String> get() = deps_versions.plugins
	val modules: Map<Pair<String, String>, String> get() = deps_versions.modules

	private val initCalled = AtomicBoolean(false)
	internal fun init() {
		if (initCalled.compareAndExchange(false, true)) {
			return // Already called before
		}
		deps_versions.init()
		VarHandle.fullFence()
	}
}
