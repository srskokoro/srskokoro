package kokoro.app

import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import build.test.assertResult
import build.test.isFailure
import io.kotest.core.spec.style.FreeSpec
import okio.FileNotFoundException
import okio.buffer

/** @see LibAssets */
class test_LibAssets : FreeSpec({
	"Test asset can be read" {
		assertResult {
			LibAssets.open("kokoro/app/sample.txt")
				.buffer().readUtf8()
		}.isSuccess().isEqualTo("foobar")
	}
	"Missing asset cannot be read" {
		assertResult {
			LibAssets.open("kokoro/app/missing.txt")
		}.isFailure<FileNotFoundException>()
	}
	test_LibAssets_platformTest()
})

/** @see test_LibAssets */
expect fun FreeSpec.test_LibAssets_platformTest()
