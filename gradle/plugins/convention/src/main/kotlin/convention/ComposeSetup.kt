package convention

import convention.internal.DependencyConsumer
import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.setUpComposeDeps(consume: DependencyConsumer) {
	consume(compose.runtime)
	consume(compose.foundation)
	consume(compose.material)
}

private val DependencyHandler.compose
	get() = extensions.getByName("compose") as org.jetbrains.compose.ComposePlugin.Dependencies
