package build.test

inline fun Process.forEachLine(action: (String) -> Unit) {
	// NOTE: The `Process` object owns its streams and should take care of
	// closing them once the process ends. We shouldn't close the returned
	// reader (or the wrapped input stream) here.
	// - See also, https://coderanch.com/t/374269/java/closing-connections-Process-streams
	inputReader().lineSequence().forEach(action)
}

fun Process.readLines(): List<String> = ArrayList<String>().also { output ->
	// NOTE: Blocks until the process ends.
	forEachLine { output.add(it) }
}

fun Process.forwardLines(): List<String> = ArrayList<String>().also { output ->
	// NOTE: Blocks until the process ends.
	forEachLine {
		println(it)
		output.add(it)
	}
}
