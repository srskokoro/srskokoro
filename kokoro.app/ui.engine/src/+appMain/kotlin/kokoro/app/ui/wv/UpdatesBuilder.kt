package kokoro.app.ui.wv

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@ArgumentsBuilderDsl
@JvmInline
value class UpdatesBuilder(@PublishedApi internal val out: StringBuilder) {

	@OptIn(ExperimentalContracts::class)
	inline fun update(id: Int, crossinline block: ArgumentsBuilder.() -> Unit) {
		contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
		out.append(','); out.append(id)
		out.append(','); out.append('[')

		val args = ArgumentsBuilder(out)
		// The following inline function call is accompanied with `crossinline`,
		// so as to not worry about non-local returns.
		block(args)

		conclude(args, ']')
	}
}
