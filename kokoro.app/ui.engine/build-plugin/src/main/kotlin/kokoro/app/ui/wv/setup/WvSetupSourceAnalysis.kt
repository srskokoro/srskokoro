package kokoro.app.ui.wv.setup

import kokoro.app.ui.wv.setup.WvSetupSourceAnalysis.*
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
		const val WV_LST = 0b01 shl 0
		const val WV_SPEC = 0b10 shl 0
		const val WV_JS = 0b11 shl 0

		const val MASK_WV_GENERAL_TYPE = 0b11 shl 0

		const val FLAG_OVERRIDE = 0b01 shl 2
		const val FLAG_SPEC_PART = 0b10 shl 2

		const val WV_JS_AS_SPEC_PART = WV_JS or FLAG_SPEC_PART
		const val MASK_WV_JS_AS_SPEC_PART = MASK_WV_GENERAL_TYPE or FLAG_SPEC_PART

		const val CONST_WV_JS = WV_JS_AS_SPEC_PART or (0b001 shl 4)
		const val TEMPL_WV_JS = WV_JS_AS_SPEC_PART or (0b010 shl 4)

		const val EXTERN_WV_JS = WV_JS or (0b011 shl 4)

		const val HEAD_WV_JS = WV_JS or (0b100 shl 4)
		const val TAIL_WV_JS = WV_JS or (0b101 shl 4)

		const val MASK_WV_SPECIALIZED_TYPE = MASK_WV_JS_AS_SPEC_PART or (0b111 shl 4)
	}

	internal object S {
		const val OVER = '!'
		const val WV = "wv"

		const val KT = "kt"
		const val JS = "js"
		const val SPEC = "spec"
		const val LST = "lst"

		const val CONST = "const"
		const val TEMPL = "templ"

		const val EXTERN = "extern"
		const val HEAD = "head"
		const val TAIL = "tail"

		const val D_WV_D = ".$WV."

		const val D_WV_KT = "$D_WV_D$KT"
		const val D_WV_JS = "$D_WV_D$JS"
		const val D_WV_SPEC = "$D_WV_D$SPEC"
		const val D_WV_LST = "$D_WV_D$LST"

		const val D_CONST_WV_JS = ".$CONST$D_WV_JS"
		const val D_TEMPL_WV_JS = ".$TEMPL$D_WV_JS"

		const val D_EXTERN_WV_JS = ".$EXTERN$D_WV_JS"
		const val D_HEAD_WV_JS = ".$HEAD$D_WV_JS"
		const val D_TAIL_WV_JS = ".$TAIL$D_WV_JS"
	}

	internal object N {
		const val OVER = "${S.OVER}".length
		const val WV = S.WV.length

		const val KT = S.KT.length
		const val JS = S.JS.length
		const val SPEC = S.SPEC.length
		const val LST = S.LST.length

		const val D_WV_D = S.D_WV_D.length

		const val D_WV_KT = S.D_WV_KT.length
		const val D_WV_JS = S.D_WV_JS.length
		const val D_WV_SPEC = S.D_WV_SPEC.length
		const val D_WV_LST = S.D_WV_LST.length

		const val D_CONST_WV_JS = S.D_CONST_WV_JS.length
		const val D_TEMPL_WV_JS = S.D_TEMPL_WV_JS.length

		const val D_EXTERN_WV_JS = S.D_EXTERN_WV_JS.length
		const val D_HEAD_WV_JS = S.D_HEAD_WV_JS.length
		const val D_TAIL_WV_JS = S.D_TAIL_WV_JS.length
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

		val content = visit.open().reader().readText()

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

				if (stamp and Stamp.FLAG_OVERRIDE != 0) {
					overrides.add(entry)
				} else when (stamp and Stamp.MASK_WV_SPECIALIZED_TYPE) {
					Stamp.CONST_WV_JS -> constParts.add(entry)
					Stamp.TEMPL_WV_JS -> templParts.add(entry)
					Stamp.WV_LST -> lstEntries.add(entry)
				}
			}
		})

		// --

		for (entry in overrides) {
			val path = entry.path

			val stamp = entry.stamp
			if (stamp and Stamp.MASK_WV_SPECIALIZED_TYPE == Stamp.CONST_WV_JS) {
				if (isSourceFiles) throw E_OverrideCannotBeConst(path, entry.sourceFile)
				continue // Skip (silently)
			}

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
	when {
		name.endsWith(S.JS, ignoreCase = true) -> when {
			name.startsWith(S.D_CONST_WV_JS, (name.length - N.D_CONST_WV_JS).also { i = it }, ignoreCase = true) -> {
				r = Stamp.CONST_WV_JS
			}
			name.startsWith(S.D_TEMPL_WV_JS, (name.length - N.D_TEMPL_WV_JS).also { i = it }, ignoreCase = true) -> {
				r = Stamp.TEMPL_WV_JS
			}
			name.startsWith(S.D_HEAD_WV_JS, (name.length - N.D_HEAD_WV_JS).also { i = it }, ignoreCase = true) -> {
				r = Stamp.HEAD_WV_JS
			}
			name.startsWith(S.D_TAIL_WV_JS, (name.length - N.D_TAIL_WV_JS).also { i = it }, ignoreCase = true) -> {
				r = Stamp.TAIL_WV_JS
			}
			name.startsWith(S.D_EXTERN_WV_JS, (name.length - N.D_EXTERN_WV_JS).also { i = it }, ignoreCase = true) -> {
				r = Stamp.EXTERN_WV_JS
			}
		}
		name.endsWith(S.SPEC, ignoreCase = true) -> when {
			name.startsWith(S.D_WV_D, (name.length - N.D_WV_SPEC).also { i = it }, ignoreCase = true) -> {
				r = Stamp.WV_SPEC
			}
		}
		name.endsWith(S.LST, ignoreCase = true) -> when {
			name.startsWith(S.D_WV_D, (name.length - N.D_WV_LST).also { i = it }, ignoreCase = true) -> {
				r = Stamp.WV_LST
			}
		}
	}
	if (r != 0) (i - 1).let {
		if (it >= 0 && name[it] == S.OVER) {
			r = r or Stamp.FLAG_OVERRIDE
		}
	}
	return r
}

private fun getFileExtLengthFromStamp(stamp: Int) = when (stamp and Stamp.MASK_WV_SPECIALIZED_TYPE) {
	Stamp.CONST_WV_JS -> N.D_CONST_WV_JS
	Stamp.TEMPL_WV_JS -> N.D_TEMPL_WV_JS

	Stamp.EXTERN_WV_JS -> N.D_EXTERN_WV_JS
	Stamp.HEAD_WV_JS -> N.D_HEAD_WV_JS
	Stamp.TAIL_WV_JS -> N.D_TAIL_WV_JS

	Stamp.WV_SPEC -> N.D_WV_SPEC
	Stamp.WV_LST -> N.D_WV_LST

	else -> 0
}


private fun E_DuplicateSourceEntry(visit: FileTreeElement) = E_DuplicateSourceEntry(visit.path, visit.file)

internal fun E_DuplicateSourceEntry(sourcePath: String, sourceFile: File) =
	DuplicateFileCopyingException("Entry is present in multiple sources: $sourcePath\n- Input file: $sourceFile")


private fun E_OverrideCannotBeConst(sourcePath: String, sourceFile: File?) =
	InvalidUserDataException("Override entry cannot be a `*${S.D_CONST_WV_JS}` file: $sourcePath\n- Input file: $sourceFile")

private fun E_MissingOverrideParent(sourcePath: String, sourceFile: File?) =
	InvalidUserDataException("Override entry doesn't override anything: $sourcePath\n- Input file: $sourceFile")

private fun E_MissingSpecParent(sourcePath: String, sourceFile: File?) =
	InvalidUserDataException("Entry must have a `*${S.D_WV_SPEC}` counterpart: $sourcePath\n- Input file: $sourceFile")
