package kokoro.app.ui.wv

import kokoro.internal.SPECIAL_USE_IN_TESTS_DEPRECATION

private val wvIdEncodeMap = charArrayOf(
	'A', 'B', 'C', 'D', /**/ 'E', 'F', 'G', 'H', /**/ 'I', 'J', 'K', 'L', /**/ 'M',
	'N', 'O', 'P', 'Q', /**/ 'R', 'S', 'T', 'U', /**/ 'V', 'W', 'X', 'Y', /**/ 'Z',

	'a', 'b', 'c', 'd', /**/ 'e', 'f', 'g', 'h', /**/ 'i', 'j', 'k', 'l', /**/ 'm',
	'n', 'o', 'p', 'q', /**/ 'r', 's', 't', 'u', /**/ 'v', 'w', 'x', 'y', /**/ 'z',

	'0', '1', '2', '3', /**/ '4', '5', '6', '7', /**/ '8', '9', '$', '_', /**/
)

private const val SLOT_BITS = 5
internal const val MAX_SLOT = (1 shl SLOT_BITS) - 1 // 31

@Deprecated(SPECIAL_USE_IN_TESTS_DEPRECATION)
internal fun `-TestAccess-wvIdEncodeMap`() = wvIdEncodeMap

internal fun appendWvElemId(sb: StringBuilder, elemId: Int) {
	// TODO Benchmark whether filling a `CharArray` first then appending would
	//  be more performant than appending each `Char` individually as below.

	val slot = elemId and MAX_SLOT
	val ecm = wvIdEncodeMap
	sb.append(ecm[slot])

	var rem = elemId ushr SLOT_BITS
	do {
		sb.append(ecm[rem and 63])
		rem = rem ushr 6
	} while (rem != 0)
}

internal fun appendWvElemId_quoted(sb: StringBuilder, elemId: Int) {
	sb.append('"')
	appendWvElemId(sb, elemId)
	sb.append('"')
}
