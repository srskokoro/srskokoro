package build.kt.jvm.app.packaged.win

import build.api.file.file
import de.undercouch.gradle.tasks.download.DownloadAction
import de.undercouch.gradle.tasks.download.DownloadSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.file.Deleter
import javax.inject.Inject

abstract class WixInstallTask : DefaultTask() {

	@get:Optional
	@get:Input
	abstract val wixBinZipUrl: Property<String>

	@get:OutputFile
	abstract val wixBinZipDestination: RegularFileProperty

	@get:OutputDirectory
	abstract val wixBinDestination: DirectoryProperty

	// --

	@get:Inject
	protected abstract val del: Deleter

	@get:Inject
	protected abstract val files: FileOperations

	@Suppress("LeakingThis")
	private val downloader = DownloadAction(project, this)

	@get:Internal
	val downloadSpec: DownloadSpec get() = downloader

	@TaskAction
	open fun install() {
		val wixBinZipDestination = wixBinZipDestination.file
		val wixBinDestination = wixBinDestination.file

		val wixBinZipUrl = wixBinZipUrl.orNull ?: kotlin.run {
			del.run {
				delete(wixBinZipDestination)
				ensureEmptyDirectory(wixBinDestination)
			}
			return // Done. Skip code below.
		}

		downloader.apply {
			src(wixBinZipUrl)
			dest(wixBinZipDestination)
		}.execute().get()

		files.sync {
			from(files.zipTree(wixBinZipDestination))
			into(wixBinDestination)
		}
	}
}
