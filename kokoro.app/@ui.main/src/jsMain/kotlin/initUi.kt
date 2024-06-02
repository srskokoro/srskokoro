@JsExport
@JsName("init")
fun initUi(uiFunction: () -> Unit) {
	uiFunction.invoke()
}
