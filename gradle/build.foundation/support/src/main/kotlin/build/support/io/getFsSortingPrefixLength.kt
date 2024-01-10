package build.support.io

fun getFsSortingPrefixLength(filename: String) = run(fun(): Int {
	// NOTE: A filename likely cannot be empty, but just in case...
	if (filename.isNotEmpty()) when (filename[0]) {
		// NOTE: Listed in sorted order.
		'!', '#', '$', '-', '@', '~' -> return 1
		// The prefix can be a custom label in brackets
		'[' -> return filename.indexOf(']', 1) + 1 // -- returns `0` if not found
	}
	return 0
})
