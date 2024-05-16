package build.test.android

import org.junit.runners.model.FrameworkMethod
import org.robolectric.RobolectricTestRunner
import org.robolectric.internal.bytecode.InstrumentationConfiguration

// Inspired from, https://github.com/LeoColman/kotest-android/blob/c636087/kotest-extensions-android/src/main/kotlin/br/com/colman/kotest/android/extensions/robolectric/ContainedRobolectricRunner.kt
internal class KotestRobolectricRunner : RobolectricTestRunner(PlaceholderTest::class.java, injector) {

	companion object {
		private val injector = defaultInjector().build()
	}

	@Suppress("JUnitMalformedDeclaration")
	class PlaceholderTest {
		@org.junit.Test
		fun testPlaceholder() = Unit
		fun bootStrapMethod() = Unit
	}

	// --

	private val placeHolderMethod: FrameworkMethod = children[0]

	@JvmField val sdkEnvironment = getSandbox(placeHolderMethod)
		.also { configureSandbox(it, placeHolderMethod) }

	private val bootStrapMethod = sdkEnvironment
		.bootstrappedClass<Any>(testClass.javaClass)
		.getMethod(PlaceholderTest::bootStrapMethod.name)

	// --

	inline fun <R> doContained(block: () -> R): R {
		// Implementation reference:
		// - https://github.com/robolectric/robolectric/blob/robolectric-4.12.1/junit/src/main/java/org/robolectric/internal/SandboxTestRunner.java#L235
		val priorClassLoader = doContained_setUp()
		try {
			doContained_beforeTest()
			try {
				return block()
			} finally {
				doContained_afterTest()
			}
		} finally {
			doContained_tearDown(priorClassLoader)
		}
	}

	fun doContained_setUp(): ClassLoader? {
		val th = Thread.currentThread()
		val priorClassLoader: ClassLoader? = th.contextClassLoader // Back up
		th.contextClassLoader = sdkEnvironment.robolectricClassLoader
		return priorClassLoader
	}

	fun doContained_tearDown(priorClassLoader: ClassLoader?) {
		try {
			super.finallyAfterTest(placeHolderMethod)
		} finally {
			Thread.currentThread().contextClassLoader = priorClassLoader
		}
	}

	fun doContained_beforeTest() {
		super.beforeTest(sdkEnvironment, placeHolderMethod, bootStrapMethod)
	}

	fun doContained_afterTest() {
		super.afterTest(placeHolderMethod, bootStrapMethod)
	}

	// --

	override fun createClassLoaderConfig(method: FrameworkMethod?): InstrumentationConfiguration {
		return InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
			.doNotAcquirePackage("io.kotest")
			.build()
	}
}
