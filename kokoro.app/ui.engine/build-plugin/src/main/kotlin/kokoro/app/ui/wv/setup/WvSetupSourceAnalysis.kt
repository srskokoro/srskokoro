package kokoro.app.ui.wv.setup

import conv.internal.support.io.UnsafeCharArrayWriter
import kokoro.app.ui.wv.setup.WvSetup.N
import kokoro.app.ui.wv.setup.WvSetup.S
import kokoro.app.ui.wv.setup.WvSetupSourceAnalysis.Stamp
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.DuplicateFileCopyingException
import org.gradle.api.file.EmptyFileVisitor
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.file.FileVisitDetails
import java.io.File
import java.util.LinkedList

internal class WvSetupSourceAnalysis {

	internal object Stamp {
		const val FLAG_OVERRIDE = 1 shl 7

		const val FLAG_LST = 1 shl 6
		const val FLAG_SPEC_PART = 1 shl 5
		const val FLAG_JS = 1 shl 4

		const val MASK_WV_TYPE = FLAG_JS or FLAG_SPEC_PART or FLAG_LST or 0b111

		const val WV_CONST_JS = FLAG_JS or FLAG_SPEC_PART or 0b111
		const val WV_TEMPL_JS = FLAG_JS or FLAG_SPEC_PART or 0b110

		const val WV_HEAD_JS = FLAG_JS or 0b101
		const val WV_TAIL_JS = FLAG_JS or 0b100
		const val WV_EXTERN_JS = FLAG_JS or 0b011

		const val WV_SPEC = 0b010
		const val WV_BASE_LST = FLAG_LST or 0b001
		const val WV_LST = FLAG_LST or 0b000
		// ^ NOTE: Even though the actual "type" stamp is zero, the overall
		// value should be nonzero, as we're using zero to mean "no stamp".
	}

	class Entry(
		val stamp: Int,
		visit: FileTreeElement,
		val sourceFile: File? = null
	) {
		val name get() = relativePath.lastName

		val path = visit.path
		val relativePath = visit.relativePath

		private var _packagePath: String? = null
		val packagePath get() = _packagePath ?: relativePath.parent.pathString.also { _packagePath = it }

		val content = run(fun(): String {
			val w = UnsafeCharArrayWriter()
			visit.open().reader().use { it.copyTo(w) }
			val b = w.buffer
			val n = run(fun(): Int {
				// Trim trailing whitespace
				for (i in w.size - 1 downTo 0) {
					if (!b[i].isWhitespace())
						return i + 1
				}
				return 0
			})
			return String(b, 0, n)
		})

		var base: Entry? = null
		var override: Entry? = null

		var constPart: Entry? = null
		var templPart: Entry? = null

		fun getEffectiveEntry(): Entry = override?.getEffectiveEntry() ?: this
	}

	val entries = HashMap<String, Entry>()
	val lstEntries = LinkedList<Entry>()

	fun loadClasspathInputFiles(classpathInputFiles: FileTree) {
		loadInputFiles(classpathInputFiles, isSourceFiles = false)
	}

	fun loadSourceInputFiles(classpathInputFiles: FileTree) {
		loadInputFiles(classpathInputFiles, isSourceFiles = true)
	}

	private fun loadInputFiles(inputFiles: FileTree, isSourceFiles: Boolean) {
		val entries = entries
		val lstEntries = lstEntries

		val overrides = LinkedList<Entry>()

		val constParts = LinkedList<Entry>()
		val templParts = LinkedList<Entry>()

		inputFiles.visit(object : EmptyFileVisitor() {
			override fun visitFile(visit: FileVisitDetails) {
				val stamp = analyzeInputFileNameForStamp(visit.name)
				val entry = Entry(stamp, visit, sourceFile = if (isSourceFiles) visit.file else null)

				if (entries.putIfAbsent(entry.path, entry) != null) {
					if (isSourceFiles) throw E_DuplicateSourceEntry(visit)
					return // Skip
				}

				when (stamp and (Stamp.MASK_WV_TYPE or Stamp.FLAG_OVERRIDE)) {
					Stamp.WV_CONST_JS -> constParts.add(entry)
					Stamp.WV_CONST_JS or Stamp.FLAG_OVERRIDE -> {
						if (isSourceFiles) throw E_OverrideCannotBeConst(entry.path, entry.sourceFile)
						return // Skip (silently)
					}
					Stamp.WV_TEMPL_JS -> templParts.add(entry)
					Stamp.WV_LST -> lstEntries.add(entry)
					else -> if (stamp and Stamp.FLAG_OVERRIDE != 0) {
						overrides.add(entry)
					}
				}
			}
		})

		// --

		for (entry in overrides) {
			val path = entry.path

			val stamp = entry.stamp
			val basePath_n = path.length - getFileExtLengthFromStamp(stamp)
			val targetPath = path.substring(0, basePath_n - N.OVER) + path.substring(basePath_n)

			val base = entries[targetPath]
			if (base != null && (base.sourceFile != null) == isSourceFiles) {
				entry.base = base
				base.override = entry
			} else if (isSourceFiles) {
				throw E_MissingOverrideParent(path, entry.sourceFile)
			}
		}

		// --

		for (entry in constParts) {
			val path = entry.path
			val basePath_n = path.length - getFileExtLengthFromStamp(entry.stamp)
			val targetPath = path.substring(0, basePath_n) + S.D_WV_SPEC

			val parent = entries[targetPath]
			if (parent != null && (parent.sourceFile != null) == isSourceFiles) {
				parent.constPart = entry
			} else if (isSourceFiles) {
				throw E_MissingSpecParent(path, entry.sourceFile)
			}
		}

		for (entry in templParts) {
			val path = entry.path
			val basePath_n = path.length - getFileExtLengthFromStamp(entry.stamp)
			val targetPath = path.substring(0, basePath_n) + S.D_WV_SPEC

			val parent = entries[targetPath]
			if (parent != null && (parent.sourceFile != null) == isSourceFiles) {
				parent.templPart = entry
			} else if (isSourceFiles) {
				throw E_MissingSpecParent(path, entry.sourceFile)
			}
		}
	}
}

private fun analyzeInputFileNameForStamp(name: String): Int {
	var i = 0
	var r = 0
	if (name.endsWith(S.D_JS, ignoreCase = true)) run {
		if (name.startsWith(S.CONST, (name.length - (N.CONST + N.D_JS)).also { i = it }, ignoreCase = true)) {
			r = Stamp.WV_CONST_JS
		} else if (name.startsWith(S.TEMPL, (name.length - (N.TEMPL + N.D_JS)).also { i = it }, ignoreCase = true)) {
			r = Stamp.WV_TEMPL_JS
		} else if (name.startsWith(S.HEAD, (name.length - (N.HEAD + N.D_JS)).also { i = it }, ignoreCase = true)) {
			r = Stamp.WV_HEAD_JS
		} else if (name.startsWith(S.TAIL, (name.length - (N.TAIL + N.D_JS)).also { i = it }, ignoreCase = true)) {
			r = Stamp.WV_TAIL_JS
		} else if (name.startsWith(S.EXTERN, (name.length - (N.EXTERN + N.D_JS)).also { i = it }, ignoreCase = true)) {
			r = Stamp.WV_EXTERN_JS
		} else {
			return@run
		}
		if (!name.startsWith(S.D_WV_H, (i - N.D_WV_H).also { i = it }, ignoreCase = true)) {
			r = 0 // Revert
		}
	} else if (name.endsWith(S.SPEC, ignoreCase = true)) {
		if (name.startsWith(S.D_WV_D, (name.length - N.D_WV_SPEC).also { i = it }, ignoreCase = true)) {
			r = Stamp.WV_SPEC
		}
	} else if (name.endsWith(S.LST, ignoreCase = true)) {
		if (name.startsWith(S.D_WV_D, (name.length - N.D_WV_LST).also { i = it }, ignoreCase = true)) {
			r = Stamp.WV_LST
		} else if (name.startsWith(S.D_WV_BASE_LST, (name.length - N.D_WV_BASE_LST).also { i = it }, ignoreCase = true)) {
			r = Stamp.WV_BASE_LST
		}
	}
	if (r != 0) (i - 1).let {
		if (it >= 0 && name[it] == S.OVER) {
			r = r or Stamp.FLAG_OVERRIDE
		}
	}
	return r
}

private fun getFileExtLengthFromStamp(stamp: Int) = when (stamp and Stamp.MASK_WV_TYPE) {
	Stamp.WV_CONST_JS -> N.D_WV_CONST_JS
	Stamp.WV_TEMPL_JS -> N.D_WV_TEMPL_JS

	Stamp.WV_HEAD_JS -> N.D_WV_HEAD_JS
	Stamp.WV_TAIL_JS -> N.D_WV_TAIL_JS
	Stamp.WV_EXTERN_JS -> N.D_WV_EXTERN_JS

	Stamp.WV_SPEC -> N.D_WV_SPEC
	Stamp.WV_BASE_LST -> N.D_WV_BASE_LST
	Stamp.WV_LST -> N.D_WV_LST

	else -> 0
}


private fun E_DuplicateSourceEntry(visit: FileTreeElement) = E_DuplicateSourceEntry(visit.path, visit.file)

internal fun E_DuplicateSourceEntry(sourcePath: String, sourceFile: File) =
	DuplicateFileCopyingException("Entry is present in multiple sources: $sourcePath\n- Input file: $sourceFile")


private fun E_OverrideCannotBeConst(sourcePath: String, sourceFile: File?) =
	InvalidUserDataException("Override entry cannot be a `*${S.D_WV_CONST_JS}` file: $sourcePath\n- Input file: $sourceFile")

private fun E_MissingOverrideParent(sourcePath: String, sourceFile: File?) =
	InvalidUserDataException("Override entry doesn't override anything: $sourcePath\n- Input file: $sourceFile")

private fun E_MissingSpecParent(sourcePath: String, sourceFile: File?) =
	InvalidUserDataException("Entry must have a `*${S.D_WV_SPEC}` counterpart: $sourcePath\n- Input file: $sourceFile")
