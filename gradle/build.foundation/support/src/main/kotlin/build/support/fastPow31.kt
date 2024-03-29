package build.support

fun fastPow31(n: Int): Int {
	val t = fastPow31Table
	if (n < t.size) return t[n]
	return fastPow31Calc(n)
}

// From,
// - https://gist.github.com/apangin/91c07684635893e3f1d5
// - https://stackoverflow.com/a/35671374
internal fun fastPow31Calc(n: Int): Int {
	var x = 31
	var y = n
	var result = if (y and 1 != 0) x else 1
	while (y > 1) {
		x *= x
		y = y shr 1
		if (y and 1 != 0) result *= x
	}
	return result
}

/**
 * Generated using [fastPow31Calc]`()`:
 * ```
 * var i = 0
 * while (i < 64) {
 *   do print("${fastPow31Calc(i++)},")
 *   while (i % 8 != 0)
 *   println()
 * }
 * ```
 *
 * @see fastPow31Table_n
 */
private val fastPow31Table = intArrayOf(
	1, 31, 961, 29791, 923521, 28629151, 887503681, 1742810335,
	-1807454463, -196513505, -1796951359, 129082719, -293403007, -505558625, 1507551809, -510534177,
	1353309697, -997072353, -844471871, -408824225, 211350913, -2038056289, 1244764481, -67006753,
	-2077209343, 31019807, 961614017, -254736545, 693101697, 11316127, 350799937, -2010103841,
	2111290369, 1025491999, 1725480897, 1950300255, 329765761, 1632803999, -922683583, 1461579999,
	-1935660287, 124073247, -448696639, -1024693921, -1700740479, -1183347297, 1970939457, 969581023,
	-7759359, -240540129, 1133190593, 769170015, -1925533311, 438009503, 693392705, 20337375,
	630458625, -1930619105, 280349889, 100911967, -1166696319, -1807847521, -208698303, 2120287199,
)

internal val fastPow31Table_n get() = fastPow31Table.size
