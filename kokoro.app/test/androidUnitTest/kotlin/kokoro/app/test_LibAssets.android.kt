package kokoro.app

import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSuccess
import build.test.assertResult
import build.test.assertWith
import build.test.isFailure
import io.kotest.core.spec.style.FreeSpec
import okio.FileNotFoundException
import okio.buffer
import okio.use

/** @see test_LibAssets */
actual fun FreeSpec.test_LibAssets_platformTest() {
	val assetInAndroidSrc = "kokoro/app/sample.android.txt"

	"Test asset under 'android' source set can be read" {
		assertResult {
			LibAssets.open(assetInAndroidSrc)
				.buffer().use { it.readUtf8() }
		}.isSuccess().isEqualTo("baz")
	}
	"Test asset cannot be retrieved as resource" {
		assertWith {
			test_LibAssets::class.java.classLoader!!
				.getResourceAsStream(assetInAndroidSrc)
				?.apply { close() }
		}.isNull()
	}

	"Resource cannot be retrieved as asset" {
		val someResPath = "kokoro/app/someRes.txt"

		// Assert that resource exists!
		assertResult {
			test_LibAssets::class.java.classLoader!!
				.getResourceAsStream(someResPath)!!
				.use { String(it.readAllBytes()) }
		}.isSuccess().isEqualTo("Hi!")

		assertResult {
			LibAssets.open(someResPath).close()
		}.isFailure<FileNotFoundException>()
	}
	"Common resource cannot be retrieved as asset" {
		val someResPath = "kokoro/app/commonRes.txt"

		// Assert that resource exists!
		assertResult {
			test_LibAssets::class.java.classLoader!!
				.getResourceAsStream(someResPath)!!
				.use { String(it.readAllBytes()) }
		}.isSuccess().isEqualTo("Hello!")

		assertResult {
			LibAssets.open(someResPath).close()
		}.isFailure<FileNotFoundException>()
	}
}
