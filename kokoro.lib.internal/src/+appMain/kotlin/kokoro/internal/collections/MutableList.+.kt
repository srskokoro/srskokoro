package kokoro.internal.collections

// From, https://github.com/cashapp/redwood/blob/0.5.0/build-support/src/main/resources/app/cash/redwood/buildsupport/composeHelpers.kt
fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int, count: Int) {
	val dest = if (fromIndex > toIndex) toIndex else toIndex - count
	if (count == 1) {
		if (fromIndex == toIndex + 1 || fromIndex == toIndex - 1) {
			// Adjacent elements, perform swap to avoid backing array manipulations.
			val fromEl = get(fromIndex)
			val toEl = set(toIndex, fromEl)
			set(fromIndex, toEl)
		} else {
			val fromEl = removeAt(fromIndex)
			add(dest, fromEl)
		}
	} else {
		val subView = subList(fromIndex, fromIndex + count)
		val subCopy = subView.toMutableList()
		subView.clear()
		addAll(dest, subCopy)
	}
}

// From, https://github.com/cashapp/redwood/blob/0.5.0/build-support/src/main/resources/app/cash/redwood/buildsupport/composeHelpers.kt
fun <T> MutableList<T>.remove(index: Int, count: Int) {
	if (count == 1) {
		removeAt(index)
	} else {
		subList(index, index + count).clear()
	}
}
