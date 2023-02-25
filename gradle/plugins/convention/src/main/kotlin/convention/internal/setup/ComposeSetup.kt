package convention.internal.setup

import convention.internal.DependencyConsumer
import org.jetbrains.compose.ComposePlugin

internal fun setUpComposeDeps(compose: ComposePlugin.Dependencies, consume: DependencyConsumer) {
	consume(compose.runtime)
	consume(compose.foundation)
	consume(compose.material)
}
