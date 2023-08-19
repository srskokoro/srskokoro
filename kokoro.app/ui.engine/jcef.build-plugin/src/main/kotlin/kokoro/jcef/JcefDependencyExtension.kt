package kokoro.jcef

abstract class JcefDependencyExtension internal constructor() : JcefExtension() {
	val platform get() = jcefBuildPlatform

	val dependency = jcefMavenDep
	val recommendedJvmArgs by lazy(LazyThreadSafetyMode.PUBLICATION) {
		if (platform.os.isMacOSX) listOf(
			"--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
			"--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED",
			"--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
		) else emptyList()
	}
}