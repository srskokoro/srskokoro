import java.lang.invoke.VarHandle
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("ClassName")
object deps {
	object bundles : deps_bundles()

	val plugins: Map<String, String> get() = deps_versions.plugins
	val pluginGroups: Map<String, String> get() = deps_versions.pluginGroups

	val modules: Map<Pair<String, String>, String> get() = deps_versions.modules
	val moduleGroups: Map<String, String> get() = deps_versions.moduleGroups

	private val initCalled = AtomicBoolean(false)
	internal fun init() {
		if (initCalled.compareAndExchange(false, true)) {
			return // Already called before
		}
		deps_versions.init()
		VarHandle.fullFence()
	}
}
