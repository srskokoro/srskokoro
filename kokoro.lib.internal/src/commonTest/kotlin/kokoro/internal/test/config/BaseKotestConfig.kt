package kokoro.internal.test.config

import io.kotest.core.config.AbstractProjectConfig

open class BaseKotestConfig : AbstractProjectConfig() {
	override val parallelism: Int? get() = 4
}
