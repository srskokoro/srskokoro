package build.conventions.internal

import build.conventions.internal.InternalConventions.TEST_TMPDIR
import build.conventions.internal.InternalConventions.env__extension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
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
	protected val env = LinkedHashMap<String, String>().also { env ->
		task.extensions.add(env__extension, env)

		// Speed up startup time for when using Kotest. See also,
		// - https://github.com/kotest/kotest/issues/3126#issuecomment-1516917534
		// - https://github.com/kotest/kotest/issues/3746#issuecomment-1807231614
		// - https://github.com/kotest/kotest/pull/2925
		env["kotest_framework_classpath_scanning_autoscan_disable"] = "true"
		env["kotest_framework_classpath_scanning_config_disable"] = "true"
		env["kotest_framework_discovery_jar_scan_disable"] = "true"
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
			env.forEach { (k, v) -> task.environment(k, v, false) }

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
			env.forEach { (k, v) -> task.environment(k, v) }

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
			env.forEach { (k, v) -> task.environment(k, v) }
			task.systemProperty("java.io.tmpdir", ioTmpDir.path)
			task.environment(TEST_TMPDIR, testTmpDir.path)
		}
	}
}

fun InternalConventions.setUpTestTasks(project: Project): Unit = with(project) {
	tasks.withType<AbstractTestTask>().configureEach {
		when (this) {
			is KotlinNativeTest -> run {
				doFirst(TestTaskSetupInDoFirst.ForNative(this))
			}
			is KotlinJsTest -> run {
				doFirst(TestTaskSetupInDoFirst.ForJs(this))
			}
			is Test -> run {
				doFirst(TestTaskSetupInDoFirst.ForJvm(this))
				useJUnitPlatform()
				jvmArgs("-ea") // Also enables stacktrace recovery for kotlinx coroutines
			}
		}
	}
}
