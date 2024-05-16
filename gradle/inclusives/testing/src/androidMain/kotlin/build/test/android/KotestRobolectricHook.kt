package build.test.android

import io.kotest.core.extensions.ConstructorExtension
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.concurrent.Callable
import kotlin.reflect.KClass
import kotlin.time.Duration

// Inspired from, https://github.com/LeoColman/kotest-android/blob/c636087/kotest-extensions-android/src/main/kotlin/br/com/colman/kotest/android/extensions/robolectric/RobolectricExtension.kt
object KotestRobolectricHook : ConstructorExtension, TestCaseExtension {
	private val refQueue = ReferenceQueue<Spec>()
	private val entries = HashMap<SpecEntry, SpecEntry>()

	private class SpecEntry : WeakReference<Spec> {
		private val hash: Int
		@JvmField val runner: KotestRobolectricRunner?

		constructor(spec: Spec) : super(spec) {
			this.hash = System.identityHashCode(spec)
			this.runner = null
		}

		constructor(
			spec: Spec, refQueue: ReferenceQueue<Spec>,
			runner: KotestRobolectricRunner,
		) : super(spec, refQueue) {
			this.hash = System.identityHashCode(spec)
			this.runner = runner
		}

		override fun equals(other: Any?): Boolean =
			this === other || (other is SpecEntry && get() === other.get())

		override fun hashCode(): Int = hash
	}

	private fun purgeStaleEntries() {
		val refQueue = refQueue
		val entries = entries
		while (true) entries.remove(refQueue.poll() ?: break)
	}

	override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
		val runner = KotestRobolectricRunner()
		val spec = runner.sdkEnvironment
			.bootstrappedClass<Spec>(clazz.java)
			.getDeclaredConstructor()
			.newInstance()

		purgeStaleEntries()
		val entry = SpecEntry(spec, refQueue, runner)
		entries[entry] = entry

		return spec
	}

	private fun runnerFor(spec: Spec): KotestRobolectricRunner {
		purgeStaleEntries()
		return entries[SpecEntry(spec)]?.runner
			?: error("Unexpected! No runner for spec: $spec")
	}

	// --

	private val IO_EXECUTOR = Dispatchers.IO.asExecutor()

	override suspend fun intercept(
		testCase: TestCase,
		execute: suspend (TestCase) -> TestResult,
	): TestResult = suspendCancellableCoroutine(fun(cont) = IO_EXECUTOR.execute {
		val th = Thread.currentThread()
		cont.invokeOnCancellation { th.interrupt() }
		cont.resumeWith(runCatching {
			val runner = runnerFor(testCase.spec)
			// `sdkEnvironment.runOnMainThread` is necessary to ensure
			// Robolectric's looper state doesn't carry over to the next test
			// class.
			runner.sdkEnvironment.runOnMainThread(Callable {
				runner.doContained {
					runBlocking(cont.context) {
						execute(testCase)
					}
				}
			})
		}.recover { ex ->
			// Without this, the whole test class will be silently skipped.
			TestResult.Error(Duration.ZERO, ex)
		})
	})
}
