package kokoro.app.ui.wv.setup

import org.gradle.api.tasks.util.PatternFilterable

internal object WvSetup {

	object S {
		const val OVER = '!'
		const val WV = "wv"

		const val KT = "kt"
		const val JS = "js"
		const val D_JS = ".$JS"
		const val SPEC = "spec"
		const val LST = "lst"
		const val D_LST = ".$LST"

		const val BASE = "base"

		const val CONST = "const"
		const val UNIT = "unit"

		const val HEAD = "head"
		const val TAIL = "tail"

		const val EXTERN = "extern"

		const val D_WV_D = ".$WV."
		const val D_WV_H = ".$WV-"

		const val D_WV_KT = "$D_WV_D$KT"
		const val D_WV_JS = "$D_WV_D$JS"
		const val D_WV_SPEC = "$D_WV_D$SPEC"
		const val D_WV_BASE_LST = "$D_WV_H$BASE$D_LST"
		const val D_WV_LST = "$D_WV_D$LST"

		const val D_WV_CONST_JS = "$D_WV_H$CONST$D_JS"
		const val D_WV_UNIT_JS = "$D_WV_H$UNIT$D_JS"

		const val D_WV_HEAD_JS = "$D_WV_H$HEAD$D_JS"
		const val D_WV_TAIL_JS = "$D_WV_H$TAIL$D_JS"

		const val D_WV_EXTERN_JS = "$D_WV_H$EXTERN$D_JS"
	}

	object N {
		const val OVER = "${S.OVER}".length
		const val WV = S.WV.length

		const val KT = S.KT.length
		const val JS = S.JS.length
		const val D_JS = S.D_JS.length
		const val SPEC = S.SPEC.length
		const val LST = S.LST.length
		const val D_LST = S.D_LST.length

		const val BASE = S.BASE.length

		const val CONST = S.CONST.length
		const val UNIT = S.UNIT.length

		const val HEAD = S.HEAD.length
		const val TAIL = S.TAIL.length

		const val EXTERN = S.EXTERN.length

		const val D_WV_D = S.D_WV_D.length
		const val D_WV_H = S.D_WV_H.length

		const val D_WV_KT = S.D_WV_KT.length
		const val D_WV_JS = S.D_WV_JS.length
		const val D_WV_SPEC = S.D_WV_SPEC.length
		const val D_WV_BASE_LST = S.D_WV_BASE_LST.length
		const val D_WV_LST = S.D_WV_LST.length

		const val D_WV_CONST_JS = S.D_WV_CONST_JS.length
		const val D_WV_UNIT_JS = S.D_WV_UNIT_JS.length

		const val D_WV_HEAD_JS = S.D_WV_HEAD_JS.length
		const val D_WV_TAIL_JS = S.D_WV_TAIL_JS.length

		const val D_WV_EXTERN_JS = S.D_WV_EXTERN_JS.length
	}

	const val WV_LST_getId = "_wv_getId(wvUnitKey: String): Int"

	fun includeJsInputs(filterable: PatternFilterable) {
		filterable.include("**/*${S.D_WV_CONST_JS}")
		filterable.include("**/*${S.D_WV_UNIT_JS}")

		filterable.include("**/*${S.D_WV_HEAD_JS}")
		filterable.include("**/*${S.D_WV_TAIL_JS}")

		filterable.include("**/*${S.D_WV_EXTERN_JS}")
	}

	fun includeExportables(filterable: PatternFilterable) {
		includeJsInputs(filterable)
		filterable.include("**/*${S.D_WV_SPEC}")
		filterable.include("**/*${S.D_WV_BASE_LST}")
	}

	fun includeAllSupported(filterable: PatternFilterable) {
		includeExportables(filterable)
		filterable.include("**/*${S.D_WV_LST}")
	}
}
