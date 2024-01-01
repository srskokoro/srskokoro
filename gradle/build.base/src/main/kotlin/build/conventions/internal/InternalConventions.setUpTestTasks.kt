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

private sealed interface TestTaskSetupInDoFirst<T : AbstractTestTask> : Action<Task> {

	override fun execute(task: Task) {
		val taskTmpDir = task.temporaryDir

		val testTmpDir = File(taskTmpDir, "x")
		testTmpDir.deleteRecursively()

		val ioTmpDir = File(taskTmpDir, "io")
		// The JVM expects this to exist (or it'll warn us). And perhaps, others
		// too is expecting this to exist.
		ioTmpDir.mkdirs()

		@Suppress("UNCHECKED_CAST")
		(task as T).execute(ioTmpDir, testTmpDir)
	}

	fun T.execute(ioTmpDir: File, testTmpDir: File)

	data object ForNative : TestTaskSetupInDoFirst<KotlinNativeTest> {

		override fun KotlinNativeTest.execute(ioTmpDir: File, testTmpDir: File) {
			extraEnvVars()?.forEach { (k, v) -> environment(k, v, false) }

			// See also,
			// - https://github.com/square/okio/blob/parent-3.7.0/okio/src/unixMain/kotlin/okio/UnixPosixVariant.kt#L55
			// - https://github.com/square/okio/blob/parent-3.7.0/okio/src/mingwX64Main/kotlin/okio/WindowsPosixVariant.kt#L52
			// - https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-gettemppatha#remarks
			// - https://web.archive.org/web/20190213135141/https://blogs.msdn.microsoft.com/oldnewthing/20150417-00/?p=44213
			ioTmpDir.path.let {
				environment("TMPDIR", it, false)
				environment("TMP", it, false)
				environment("TEMP", it, false)
			}

			environment(TEST_TMPDIR, testTmpDir.path, false)
		}
	}

	data object ForJs : TestTaskSetupInDoFirst<KotlinJsTest> {

		override fun KotlinJsTest.execute(ioTmpDir: File, testTmpDir: File) {
			extraEnvVars()?.forEach { (k, v) -> environment(k, v) }

			ioTmpDir.path.let {
				environment("TMPDIR", it)
				environment("TMP", it)
				environment("TEMP", it)
			}

			environment(TEST_TMPDIR, testTmpDir.path)
		}
	}

	data object ForJvm : TestTaskSetupInDoFirst<Test> {

		override fun Test.execute(ioTmpDir: File, testTmpDir: File) {
			extraEnvVars()?.forEach { (k, v) -> environment(k, v) }
			systemProperty("java.io.tmpdir", ioTmpDir.path)
			environment(TEST_TMPDIR, testTmpDir.path)
		}
	}
}

@Suppress("UNCHECKED_CAST")
internal fun AbstractTestTask.extraEnvVars() =
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	extensions.findByName(env__extension) as Map<String, String>?

fun InternalConventions.setUpTestTasks(project: Project): Unit = with(project) {
	tasks.withType<AbstractTestTask>().configureEach {
		when (this) {
			is KotlinNativeTest -> {
				doFirst(TestTaskSetupInDoFirst.ForNative)
			}
			is KotlinJsTest -> {
				doFirst(TestTaskSetupInDoFirst.ForJs)
			}
			is Test -> {
				doFirst(TestTaskSetupInDoFirst.ForJvm)
				useJUnitPlatform()
				jvmArgs("-ea") // Also enables stacktrace recovery for kotlinx coroutines
			}
		}
	}
}
