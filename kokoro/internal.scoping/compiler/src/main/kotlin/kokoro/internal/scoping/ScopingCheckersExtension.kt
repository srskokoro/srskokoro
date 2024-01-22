package kokoro.internal.scoping

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

class ScopingCheckersExtension(session: FirSession) : FirAdditionalCheckersExtension(session) {
	override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
		// TODO
	}
}
