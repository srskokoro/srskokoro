package build.foundation

import build.foundation.BuildFoundation.TEST_TMPDIR
import build.foundation.BuildFoundation.env__extension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import java.io.File

private sealed class TestTaskSetupInDoFirst<T : AbstractTestTask>(task: T) : Action<Task> {

	// NOTE: We must set this up now, because `extensions` isn't supported by
	// the configuration cache, i.e., accessing `extensions` during execution
	// time will fail.
	protected val env = LinkedHashMap<String, Any>().also { env ->
		task.extensions.add(env__extension, env)

		// Speed up startup time for when using Kotest. See also,
		// - https://github.com/kotest/kotest/issues/3126#issuecomment-1516917534
		// - https://github.com/kotest/kotest/issues/3746#issuecomment-1807231614
		// - https://github.com/kotest/kotest/pull/2925
		env["kotest_framework_classpath_scanning_autoscan_disable"] = "true"
		env["kotest_framework_classpath_scanning_config_disable"] = "true"
		env["kotest_framework_discovery_jar_scan_disable"] = "true"

		// See also, https://stackoverflow.com/a/52629195
		env["kotest_framework_parallelism"] =
			Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
	}

	override fun execute(task: Task) {
		val taskTmpDir = task.temporaryDir

		val testTmpDir = File(taskTmpDir, "x")
		testTmpDir.deleteRecursively()

		val ioTmpDir = File(taskTmpDir, "io")
		// The JVM expects this to exist (or it'll warn us). And perhaps, others
		// too is expecting this to exist.
		ioTmpDir.mkdirs()

		@Suppress("UNCHECKED_CAST")
		execute(task as T, ioTmpDir, testTmpDir)
	}

	abstract fun execute(task: T, ioTmpDir: File, testTmpDir: File)

	class ForNative(task: KotlinNativeTest) : TestTaskSetupInDoFirst<KotlinNativeTest>(task) {

		override fun execute(task: KotlinNativeTest, ioTmpDir: File, testTmpDir: File) {
			for ((k, p) in env) resolveString(p)?.let { v -> task.environment(k, v, false) }

			// See also,
			// - https://github.com/square/okio/blob/parent-3.7.0/okio/src/unixMain/kotlin/okio/UnixPosixVariant.kt#L55
			// - https://github.com/square/okio/blob/parent-3.7.0/okio/src/mingwX64Main/kotlin/okio/WindowsPosixVariant.kt#L52
			// - https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-gettemppatha#remarks
			// - https://web.archive.org/web/20190213135141/https://blogs.msdn.microsoft.com/oldnewthing/20150417-00/?p=44213
			ioTmpDir.path.let {
				task.environment("TMPDIR", it, false)
				task.environment("TMP", it, false)
				task.environment("TEMP", it, false)
			}

			task.environment(TEST_TMPDIR, testTmpDir.path, false)
		}
	}

	class ForJs(task: KotlinJsTest) : TestTaskSetupInDoFirst<KotlinJsTest>(task) {

		override fun execute(task: KotlinJsTest, ioTmpDir: File, testTmpDir: File) {
			for ((k, p) in env) resolveString(p)?.let { v -> task.environment(k, v) }

			ioTmpDir.path.let {
				task.environment("TMPDIR", it)
				task.environment("TMP", it)
				task.environment("TEMP", it)
			}

			task.environment(TEST_TMPDIR, testTmpDir.path)
		}
	}

	class ForJvm(task: Test) : TestTaskSetupInDoFirst<Test>(task) {

		override fun execute(task: Test, ioTmpDir: File, testTmpDir: File) {
			for ((k, p) in env) resolveString(p)?.let { v -> task.environment(k, v) }

			task.systemProperty("java.io.tmpdir", ioTmpDir.path)
			task.environment(TEST_TMPDIR, testTmpDir.path)

			// NOTE: At the moment, Kotest on JVM only reads the system property
			// for this and ignores the equivalent environment variable.
			resolveString(env["kotest_framework_parallelism"])?.let {
				task.systemProperty("kotest.framework.parallelism", it)
			}
		}
	}

	companion object {
		internal tailrec fun resolveString(provider: Any?): String? =
			if (provider !is Provider<*>) provider?.toString()
			else resolveString(provider.orNull)
	}
}

fun BuildFoundation.setUpTestTasks(project: Project): Unit = project.afterEvaluate(fun(project) {
	project.tasks.withType<AbstractTestTask>().configureEach {
		when (this) {
			is KotlinNativeTest -> run {
				doFirst(TestTaskSetupInDoFirst.ForNative(this)) // -- sets up `env` extension
				doTestGivenProjectExtra("TEST_KN")
			}
			is KotlinJsTest -> run {
				doFirst(TestTaskSetupInDoFirst.ForJs(this))
				doTestGivenProjectExtra("TEST_KJS")
			}
			is Test -> run {
				doFirst(TestTaskSetupInDoFirst.ForJvm(this))
				useJUnitPlatform()
				jvmArgs("-ea") // Also enables stacktrace recovery for kotlinx coroutines
			}
		}
	}
})

private fun AbstractTestTask.doTestGivenProjectExtra(extraName: String) {
	if (!project.extra.parseBoolean(extraName, true)) {
		// Skip test task.
		//
		// NOTE: Can't use `onlyIf` as it'll skip only *this* task.
		// - See, https://stackoverflow.com/q/16214865
		disableRecursively()
	}
}

private fun Task.disableRecursively() {
	enabled = false
	setDependsOn(emptyList<Any?>())

	for (d in taskDependencies.getDependencies(this)) {
		d.disableRecursively()
	}
}
