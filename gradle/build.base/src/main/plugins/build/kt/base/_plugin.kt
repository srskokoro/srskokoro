package build.kt.base

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.kotlin
import build.api.dsl.accessors.kotlinSourceSets
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import java.io.File

class _plugin : ProjectPlugin({
	xs().add("kotlinSourceSets", kotlin.kotlinSourceSets)

	tasks.withType<AbstractTestTask>().configureEach {
		val taskTmpDir = temporaryDir
		val ioTmpDir = File(taskTmpDir, "io")

		when (this) {
			is KotlinNativeTest -> {
				// See also,
				// - https://github.com/square/okio/blob/parent-3.7.0/okio/src/unixMain/kotlin/okio/UnixPosixVariant.kt#L55
				// - https://github.com/square/okio/blob/parent-3.7.0/okio/src/mingwX64Main/kotlin/okio/WindowsPosixVariant.kt#L52
				// - https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-gettemppatha#remarks
				// - https://web.archive.org/web/20190213135141/https://blogs.msdn.microsoft.com/oldnewthing/20150417-00/?p=44213
				ioTmpDir.path.let {
					environment("TMPDIR", it)
					environment("TMP", it)
					environment("TEMP", it)
				}
			}
			is KotlinJsTest -> {
				ioTmpDir.path.let {
					environment("TMPDIR", it)
					environment("TMP", it)
					environment("TEMP", it)
				}
			}
			is Test -> {
				systemProperty("java.io.tmpdir", ioTmpDir.path)
			}
		}

		// The JVM expects this to exist (or it'll warn us), and perhaps others
		// too is expecting that.
		ioTmpDir.mkdir()
	}
})
