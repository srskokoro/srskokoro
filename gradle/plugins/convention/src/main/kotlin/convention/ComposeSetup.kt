package convention

import convention.internal.DependencyConsumer
import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.setUpComposeDeps(consume: DependencyConsumer) {
	consume(compose.runtime)
	consume(compose.foundation)
	consume(compose.material)
}

/** Needed only for preview. */
internal fun DependencyHandler.setUpComposePreviewDeps(consume: DependencyConsumer) {
	consume(compose.preview)
}

private val DependencyHandler.compose
	get() = extensions.getByName("compose") as org.jetbrains.compose.ComposePlugin.Dependencies
