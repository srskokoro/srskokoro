import java.security.MessageDigest
import java.util.HexFormat

plugins {
	id("build.conventions")
}

//#region Complex build setup

internal abstract class ChecksumDepsCoder : DefaultTask() {

	companion object {
		private const val SRC_PACKAGE = "build.dependencies"
		private const val SRC_DIR = "build/dependencies"
		private const val SRC_NAME = "DepsCoder"

		private const val SRC_PATH = "src/main/kotlin/$SRC_DIR/$SRC_NAME.kt"

		private const val TASK_NAME = "checksum$SRC_NAME"
		private const val GEN_NAME = "${SRC_NAME}_CHECKSUM"
		private const val GEN_DIR = "generated/$TASK_NAME"

		private const val MD_ALGORITHM = "SHA-1"

		fun registerInto(tasks: TaskContainer) =
			tasks.register<ChecksumDepsCoder>(TASK_NAME)
	}

	@get:InputFile
	val inputFile: File = project.file(SRC_PATH)

	@get:OutputDirectory
	val outputDir: Provider<Directory> = project.layout.buildDirectory.dir(GEN_DIR)

	@get:Inject
	abstract val fs: FileSystemOperations

	@TaskAction
	fun run() {
		val outputDir = outputDir.get().asFile
		fs.delete { delete(outputDir) }

		val target = File(outputDir, "$SRC_DIR/$GEN_NAME.kt")
		target.parentFile.mkdirs()

		val md = MessageDigest.getInstance(MD_ALGORITHM)
		inputFile.forEachBlock { buffer, bytesRead ->
			md.update(buffer, 0, bytesRead)
		}
		val d = md.digest()
		val h = HexFormat.of().formatHex(d)

		target.writeText("""
			package $SRC_PACKAGE

			/**
			 * The $MD_ALGORITHM checksum of `$SRC_NAME.kt`, the source file expected to hold
			 * [DepsEncoder] and [DepsDecoder].
			 */
			internal const val $GEN_NAME = "$h"
		""".trimIndent() + '\n')
	}
}

kotlin.sourceSets {
	main {
		kotlin.srcDir(ChecksumDepsCoder.registerInto(project.tasks))
	}
}

//#endregion

dependencies {
	implementation(project(":support"))
	testImplementation(project(":testing"))
	testImplementation(kotlin("test"))
}
