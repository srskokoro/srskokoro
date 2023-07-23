package conv.redwood.ui.wv.setup

private val wvIdEncodeMap = charArrayOf(
	'A', 'B', 'C', 'D', /**/ 'E', 'F', 'G', 'H', /**/ 'I', 'J', 'K', 'L', /**/ 'M',
	'N', 'O', 'P', 'Q', /**/ 'R', 'S', 'T', 'U', /**/ 'V', 'W', 'X', 'Y', /**/ 'Z',

	'a', 'b', 'c', 'd', /**/ 'e', 'f', 'g', 'h', /**/ 'i', 'j', 'k', 'l', /**/ 'm',
	'n', 'o', 'p', 'q', /**/ 'r', 's', 't', 'u', /**/ 'v', 'w', 'x', 'y', /**/ 'z',

	'0', '1', '2', '3', /**/ '4', '5', '6', '7', /**/ '8', '9', '$', '_', /**/
)

internal fun appendWvSetupId(sb: StringBuilder, setupId: Int) {
	val ecm = wvIdEncodeMap
	var rem = setupId
	do {
		sb.append(ecm[rem and 63])
		rem = rem ushr 6
	} while (rem != 0)
}

internal fun appendWvSetupId_quoted(sb: StringBuilder, setupId: Int) {
	sb.append('"')
	appendWvSetupId(sb, setupId)
	sb.append('"')
}
