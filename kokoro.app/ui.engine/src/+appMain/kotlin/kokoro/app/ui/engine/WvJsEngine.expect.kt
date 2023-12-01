package kokoro.app.ui.engine

expect class WvJsEngine

// TODO! Remove `kokoro.app.ui.wv.JsEvaluator` as it has been replaced already
expect fun WvJsEngine.evaluateJs(script: String)
