package build.support.io

import build.api.provider.RunUntrackedHelper
import build.api.provider.UntrackedScope
import build.plugins.test.io.TestTemp
import java.io.File
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class test_transformFileAtomic {

	companion object {
		@Suppress("DEPRECATION_ERROR")
		private inline val SCOPE: UntrackedScope
			get() = RunUntrackedHelper.DefaultUntrackedScope

		private fun assertSameModMs(source: File, destination: File) {
			assertEquals(source.lastModified(), destination.lastModified(), "Generated file should have the same timestamp as source file")
		}
	}

	@Test fun `Verify correctness`() {
		val parent = TestTemp.from(this).initDirs()
		val source = File(parent, "source.dat")
		val destination = File(parent, "destination.dat")

		assertTrue(source.createNewFile(), "Failed to create source file")
		assertTrue(source.setLastModified(System.currentTimeMillis() - 20_000), "Failed to set up source file")

		assertFalse(destination.exists(), "Destination file shouldn't exist yet")

		// --
		assertTrue("Should generate destination file") {
			SCOPE.transformFileAtomic(source, destination) {
				it.write(ByteBuffer.wrap("foo".encodeToByteArray()))
			}
		}
		assertTrue("Destination file should exist") {
			destination.exists()
		}
		assertSameModMs(source, destination)

		// --
		assertFalse("Should preserve destination file") {
			SCOPE.transformFileAtomic(source, destination) {
				fail("Expected destination file to be preserved.")
			}
		}
		assertTrue("Destination file should still exist") {
			destination.exists()
		}
		assertSameModMs(source, destination)

		// --
		destination.writeText("bar") // Modify generated file
		val destinationModMs_new = destination.lastModified()
		assertTrue("Should regenerate destination file") {
			SCOPE.transformFileAtomic(source, destination) {
				it.write(ByteBuffer.wrap("foo".encodeToByteArray()))
			}
		}
		assertSameModMs(source, destination)
		assertNotEquals(destinationModMs_new, destination.lastModified())

		// --
		val sourceModMs_old = source.lastModified()
		source.writeText("bar") // Modify source file
		Thread.sleep(1) // Needed to ensure source file has mod time < now
		assertTrue("Should regenerate destination file") {
			SCOPE.transformFileAtomic(source, destination) {
				it.write(ByteBuffer.wrap("foobar".encodeToByteArray()))
			}
		}
		assertSameModMs(source, destination)
		assertNotEquals(sourceModMs_old, source.lastModified())

		// --
		assertTrue("Source file should still exist") { source.exists() }
		assertTrue("Destination file should still exist") { destination.exists() }
	}
}
