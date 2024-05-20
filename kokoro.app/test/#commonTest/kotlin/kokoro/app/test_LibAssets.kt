package kokoro.app

import assertk.assertAll
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import assertk.assertions.matchesPredicate
import build.test.assertResult
import build.test.assertWith
import build.test.isFailure
import io.kotest.core.spec.style.FreeSpec
import okio.FileNotFoundException
import okio.buffer
import okio.use

/** @see LibAssets */
class test_LibAssets : FreeSpec({
	"Test asset can be read" {
		assertResult {
			LibAssets.open("kokoro/app/sample.txt")
				.buffer().use { it.readUtf8() }
		}.isSuccess().isEqualTo("foobar")
	}
	"Missing asset cannot be read" {
		assertResult {
			LibAssets.open("kokoro/app/missing.txt").close()
		}.isFailure<FileNotFoundException>()
	}
	"Asset directories cannot be streamed" {
		assertAll {
			for (path in listOf("kokoro/app", "kokoro/app/")) assertWith {
				LibAssets.openOrNull(path)
			}.matchesPredicate { source ->
				source == null || source.buffer().use { it.readUtf8() }.trim() == ""
			}
		}
	}
	test_LibAssets_platformTest()
})

/** @see test_LibAssets */
expect fun FreeSpec.test_LibAssets_platformTest()
