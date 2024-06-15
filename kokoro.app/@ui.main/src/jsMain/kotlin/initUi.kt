import kokoro.app.ui.engine.UiStates
import kokoro.app.ui.engine.handleUiStates
import kotlinx.serialization.json.Json

@JsExport
@JsName("init")
fun initUi(uiFunction: () -> Unit) {
	// NOTE: The call below is expected to throw if already called before.
	val uiStates = handleUiStates { JSON.stringify(UiStates.encode()) }

	val uiStatesFormat = Json // TODO!
	UiStates.init(uiStatesFormat, JSON.parse(uiStates))

	uiFunction.invoke()
}
