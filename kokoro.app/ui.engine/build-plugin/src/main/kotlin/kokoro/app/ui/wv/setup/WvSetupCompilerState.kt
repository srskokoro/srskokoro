package kokoro.app.ui.wv.setup

import kokoro.app.ui.wv.setup.WvSetup.S
import kokoro.app.ui.wv.setup.WvSetupSourceAnalysis.Entry
import kokoro.app.ui.wv.setup.WvSetupSourceAnalysis.Stamp
import org.gradle.api.InvalidUserDataException
import java.util.LinkedList

internal class WvSetupCompilerState(val lst: Entry, private val entries: Map<String, Entry>) {

	val externEntries = LinkedList<Entry>()

	val headEntries = LinkedList<Entry>()
	val tailEntries = LinkedList<Entry>()

	val packageEntries = LinkedHashMap<String, PackageEntry>()

	class PackageEntry {
		val constEntries = LinkedList<Entry>()
		val templEntries = LinkedList<Entry>()
	}

	init {
		loadLst(lst.getEffectiveEntry(), HashSet())
	}

	private fun loadLst(context: Entry, seenPaths: MutableSet<String>) {
		val entries = entries
		var ln = 0
		for (line in context.content.lineSequence()) {
			++ln

			if (line.isBlank() || line.startsWith('#')) continue

			if (line.startsWith("include-base")) {
				context.base?.let { loadLst(it, seenPaths) }
				continue // Skip code below
			}

			if (!line.startsWith(IMPORT_DIRECTIVE)) throw E_UnknownDirective(context, ln, line)
			val path = parseImportDirectiveForPath(line)
			val entry = entries[path] ?: throw E_ReferencedEntryNotFound(context, ln, path)

			when (entry.stamp and (Stamp.MASK_WV_TYPE or Stamp.FLAG_OVERRIDE)) {
				Stamp.WV_BASE_LST -> {
					if (seenPaths.add(path)) {
						loadLst(entry.getEffectiveEntry(), seenPaths)
					} else {
						throw E_DuplicateBaseLstNotAllowed(context, ln)
					}
				}

				Stamp.WV_SPEC -> {
					if (seenPaths.add(path)) {
						loadSpec(entry.getEffectiveEntry(), seenPaths)
					}
				}

				Stamp.WV_HEAD_JS -> {
					if (seenPaths.add(path)) {
						headEntries.addLast(entry)
					} else {
						throw E_DuplicateHeadNotAllowed(context, ln)
					}
				}
				Stamp.WV_TAIL_JS -> {
					if (seenPaths.add(path)) {
						tailEntries.addLast(entry)
					} else {
						throw E_DuplicateTailNotAllowed(context, ln)
					}
				}

				Stamp.WV_EXTERN_JS -> {
					if (seenPaths.add(path)) {
						externEntries.addLast(entry)
					}
				}

				else -> throw E_UnsupportedEntryInLst(context, ln, path)
			}
		}
	}

	private fun loadSpec(context: Entry, seenPaths: MutableSet<String>) {
		packageEntries.computeIfAbsent(context.packagePath) { PackageEntry() }.let { packageEntry ->
			context.constPart?.let { packageEntry.constEntries.addLast(it) }
			context.templPart?.let { packageEntry.templEntries.addLast(it) }
		}

		val entries = entries
		var ln = 0
		for (line in context.content.lineSequence()) {
			++ln

			if (line.isBlank() || line.startsWith('#')) continue

			if (line.startsWith("include-base")) {
				context.base?.let { loadSpec(it, seenPaths) }
				continue // Skip code below
			}

			if (!line.startsWith(IMPORT_DIRECTIVE)) throw E_UnknownDirective(context, ln, line)
			val path = parseImportDirectiveForPath(line)
			val entry = entries[path] ?: throw E_ReferencedEntryNotFound(context, ln, path)

			if (entry.stamp and (Stamp.MASK_WV_TYPE or Stamp.FLAG_OVERRIDE) != Stamp.WV_SPEC) {
				throw E_UnsupportedEntryInSpec(context, ln, path)
			}

			if (seenPaths.add(path)) {
				loadSpec(entry.getEffectiveEntry(), seenPaths)
			}
		}
	}
}

private const val IMPORT_DIRECTIVE = "import "
private const val IMPORT_DIRECTIVE_n = IMPORT_DIRECTIVE.length

private fun parseImportDirectiveForPath(line: String) = line.substring(
	IMPORT_DIRECTIVE_n,
	line.indexOfLast { !it.isWhitespace() }.coerceAtLeast(0)
)


private fun E_UnknownDirective(context: Entry, ln: Int, line: String) = InvalidUserDataException(
	"Error: ${context.sourceFile}:$ln:1 Unknown directive for line: $line"
)

private fun E_ReferencedEntryNotFound(context: Entry, ln: Int, path: String) = InvalidUserDataException(
	"Error: ${context.sourceFile}:$ln:1 Referenced entry not found: $path"
)


private fun E_DuplicateBaseLstNotAllowed(context: Entry, ln: Int) = InvalidUserDataException(
	"Error: ${context.sourceFile}:$ln:1 Duplicate `*${S.D_WV_BASE_LST}` entries not allowed"
)

private fun E_DuplicateHeadNotAllowed(context: Entry, ln: Int) = InvalidUserDataException(
	"Error: ${context.sourceFile}:$ln:1 Duplicate `*${S.D_WV_HEAD_JS}` entries not allowed"
)

private fun E_DuplicateTailNotAllowed(context: Entry, ln: Int) = InvalidUserDataException(
	"Error: ${context.sourceFile}:$ln:1 Duplicate `*${S.D_WV_TAIL_JS}` entries not allowed"
)


private fun E_UnsupportedEntryInLst(context: Entry, ln: Int, path: String) = InvalidUserDataException(
	"Error: ${context.sourceFile}:$ln:1 Unsupported entry in `*${S.D_LST}` file: $path"
)

private fun E_UnsupportedEntryInSpec(context: Entry, ln: Int, path: String) = InvalidUserDataException(
	"Error: ${context.sourceFile}:$ln:1 Unsupported entry in `*${S.D_WV_SPEC}` file: $path"
)
