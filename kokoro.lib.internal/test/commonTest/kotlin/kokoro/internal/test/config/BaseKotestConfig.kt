package kokoro.internal.test.config

import io.kotest.core.config.AbstractProjectConfig

/**
 * @see io.kotest.core.config.ProjectConfiguration
 * @see io.kotest.core.config.Defaults
 */
open class BaseKotestConfig : AbstractProjectConfig() {
	override val parallelism: Int? get() = 4
}
