import kotlin.experimental.ExperimentalNativeApi

actual open class KotestConfig : BaseKotestConfig() {
	override val parallelism
		@OptIn(ExperimentalNativeApi::class)
		get() = Platform.getAvailableProcessors() // WARNING: May throw `IllegalStateException`
			.coerceAtLeast(2) // See, https://github.com/Kotlin/kotlinx.coroutines/pull/3366/files
}
