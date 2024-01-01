package build.conventions.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import java.io.File

/**
 * A custom temporary directory acting as a sandbox for the test task to play in
 * without fear, for when doing tests with the filesystem.
 */
// NOTE: A utility somewhere in our build uses this and expects it to be set up
// automatically.
private const val TEST_TMPDIR = "TEST_TMPDIR"

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
			systemProperty("java.io.tmpdir", ioTmpDir.path)
			environment(TEST_TMPDIR, testTmpDir.path)
		}
	}
}

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
