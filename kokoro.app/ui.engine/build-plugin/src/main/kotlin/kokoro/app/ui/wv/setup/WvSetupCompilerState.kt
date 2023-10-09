package kokoro.app.ui.wv.setup

import kokoro.app.ui.wv.setup.WvSetupSourceAnalysis.*
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

			if (line.isBlank()) continue
			if (line.startsWith('#')) {
				if (line.startsWith("%include-base", 1)) {
					context.base?.let { loadLst(it, seenPaths) }
				}
				continue // Skip code below
			}

			val path = parseInputLineForPath(line)
			val entry = entries[path]
				?: throw InvalidUserDataException("Error: ${context.sourceFile}:$ln:1 Referenced entry not found")

			when (entry.stamp and (Stamp.MASK_WV_TYPE or Stamp.FLAG_OVERRIDE)) {
				Stamp.WV_SPEC -> {
					if (seenPaths.add(path)) {
						loadSpec(entry.getEffectiveEntry(), seenPaths)
					}
				}

				Stamp.WV_HEAD_JS -> {
					if (seenPaths.add(path)) {
						headEntries.addLast(entry)
					} else {
						throw InvalidUserDataException("Error: ${context.sourceFile}:$ln:1 Duplicate `*${S.D_WV_HEAD_JS}` entries not allowed")
					}
				}
				Stamp.WV_TAIL_JS -> {
					if (seenPaths.add(path)) {
						tailEntries.addLast(entry)
					} else {
						throw InvalidUserDataException("Error: ${context.sourceFile}:$ln:1 Duplicate `*${S.D_WV_TAIL_JS}` entries not allowed")
					}
				}

				Stamp.WV_EXTERN_JS -> {
					if (seenPaths.add(path)) {
						externEntries.addLast(entry)
					}
				}

				else -> {
					throw InvalidUserDataException("Error: ${context.sourceFile}:$ln:1 Unsupported entry in `*${S.D_WV_LST}` file")
				}
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

			if (line.isBlank()) continue
			if (line.startsWith('#')) {
				if (line.startsWith("%include-base", 1)) {
					context.base?.let { loadSpec(it, seenPaths) }
				}
				continue // Skip code below
			}

			val path = parseInputLineForPath(line)
			val entry = entries[path]
				?: throw InvalidUserDataException("Error: ${context.sourceFile}:$ln:1 Referenced entry not found")

			if (entry.stamp and (Stamp.MASK_WV_TYPE or Stamp.FLAG_OVERRIDE) != Stamp.WV_SPEC) {
				throw InvalidUserDataException("Error: ${context.sourceFile}:$ln:1 Unsupported entry in `*${S.D_WV_SPEC}` file")
			}

			if (seenPaths.add(path)) {
				loadSpec(entry.getEffectiveEntry(), seenPaths)
			}
		}
	}
}

@Suppress("NOTHING_TO_INLINE")
private inline fun parseInputLineForPath(line: String) = line.trimEnd()
