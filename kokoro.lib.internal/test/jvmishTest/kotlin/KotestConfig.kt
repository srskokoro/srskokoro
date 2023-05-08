
actual class KotestConfig : BaseKotestConfig() {
	// See also, https://stackoverflow.com/a/52629195
	override val parallelism get() = Runtime.getRuntime().availableProcessors()
}
