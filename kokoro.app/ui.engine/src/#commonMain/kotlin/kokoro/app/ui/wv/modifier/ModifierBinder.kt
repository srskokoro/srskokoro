package kokoro.app.ui.wv.modifier

import kokoro.app.ui.wv.ArgumentsBuilder
import kokoro.app.ui.wv.ArgumentsBuilderDsl
import kokoro.app.ui.wv.conclude
import kotlin.jvm.JvmInline

@ArgumentsBuilderDsl
@JvmInline
value class ModifierBinder(@PublishedApi internal val out: StringBuilder) {

	inline fun bind(mId: Int, crossinline onBind: ArgumentsBuilder.() -> Unit) {
		out.append(','); out.append(mId)
		out.append(','); out.append('[')

		val args = ArgumentsBuilder(out)
		// The following inline function call is accompanied with `crossinline`,
		// so as to not worry about non-local returns.
		onBind(args)

		args.conclude(']')
	}
}
