@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.initialization.Settings
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

/**
 * Generates a 'gradle.properties' file for the specified project directory and
 * fills it with the same entries as those in the current build's
 * 'gradle.properties' file (located in the build's settings directory).
 *
 * @param projectDir the project directory path, relative to the build's
 * settings directory.
 *
 * @see Settings.getSettingsDir
 */
fun Settings.shareGradleProperties(projectDir: String, overrides: (Properties) -> Unit = {}) {
	val settingsDir = settingsDir
	val src = File(settingsDir, "gradle.properties")
	val target = File(settingsDir, "$projectDir/gradle.properties")
	val targetPath = target.toPath()

	if (target.isFile) {
		val targetAttr = Files.readAttributes(targetPath, BasicFileAttributes::class.java)
		val targetModMs = targetAttr.lastModifiedTime().toMillis()
		// Check if the target was generated after the source's modification,
		// and that the target was not tampered since its generation.
		if (targetModMs > src.lastModified() && targetModMs == targetAttr.creationTime().toMillis()) {
			return // It's likely up-to-date
		}
	}

	val props = Properties()

	// NOTE: 'gradle.properties' files are supposed to be encoded in ISO-8859-1
	// (also known as Latin-1).
	// - See, https://github.com/gradle/gradle/issues/13741#issuecomment-658177619
	// - See also, https://en.wikipedia.org/wiki/.properties
	src.inputStream().use {
		props.load(it)
	}

	overrides.invoke(props)

	// Output to a temporary file first
	val tmp = File("${target.path}.tmp")
	val tmpPath = tmp.toPath()

	// Let the following throw!
	Files.deleteIfExists(tmpPath)

	tmp.outputStream().use {
		props.store(it, "Auto-generated. DO NOT MODIFY.")
		// Necessary since file writes can be delayed by the OS (even on
		// properly closed streams) and we have to do a rename/move operation
		// later to atomically publish our changes.
		it.fd.sync()
		// ^ Same as in `androidx.core.util.AtomicFile.finishWrite()`
	}

	// Set up the file timestamps for our custom up-to-date check
	tmpPath.setModTimeAsCreateTime()

	// Atomically publish our changes via a rename/move operation
	Files.move(tmpPath, targetPath, ATOMIC_MOVE, REPLACE_EXISTING)
	// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`

	println("Auto-generated 'gradle.properties' file: ${target.absolutePath}")
}

private fun Path.setModTimeAsCreateTime() {
	val attrView = Files.getFileAttributeView(this, BasicFileAttributeView::class.java)

	// Sets the creation time to be the same as the last modification time.
	val lastModifiedTime = attrView.readAttributes().lastModifiedTime()
	attrView.setTimes(null, null, /* createTime = */ lastModifiedTime)

	// Sets the last modification time to be the same as the creation time, that
	// is, if necessary.
	//
	// Needed since the creation time may have less granularity than the last
	// modification time, and so, the former have likely been rounded to the
	// nearest supported value, making it different from the latter. This hack
	// fixes that.
	//
	// ASSUMPTION: Usually, the creation time has a higher granularity than the
	// last modification time, so the following usually won't be needed. See
	// also, https://learn.microsoft.com/en-us/windows/win32/sysinfo/file-times
	//
	val createTime = attrView.readAttributes().creationTime()
	if (createTime != lastModifiedTime) {
		attrView.setTimes(/* lastModifiedTime = */ createTime, null, null)
	}
}
