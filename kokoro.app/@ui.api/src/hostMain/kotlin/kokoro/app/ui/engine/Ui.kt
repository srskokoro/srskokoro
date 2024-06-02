package kokoro.app.ui.engine

import kokoro.app.ui.engine.web.HtmlTitleSpec
import kokoro.app.ui.engine.web.WebAsset
import kokoro.app.ui.engine.web.WebAssetSpec
import kokoro.app.ui.engine.web.plus

abstract class Ui {

	/**
	 * NOTE: This can be an empty string if it wasn't provided.
	 */
	val url: String

	val spec: WebAssetSpec

	val html: WebAsset

	val uiFqn get() = spec.query(UiSpec.PROP_UI)

	/**
	 * TIP: Use an empty string for [url] if it isn't needed.
	 */
	constructor(url: String, spec: UiSpec, extras: WebAssetSpec?) {
		this.url = url
		@Suppress("NAME_SHADOWING")
		var spec: WebAssetSpec = spec
		if (extras != null) spec += extras
		this.spec = spec
		this.html = spec + UiTemplate.BASE
	}

	constructor(url: String, spec: UiSpec) {
		this.url = url
		this.spec = spec
		this.html = spec + UiTemplate.BASE
	}

	constructor(url: String) {
		this.url = url
		val spec = UiSpec()
		this.spec = spec
		this.html = spec + UiTemplate.BASE
	}

	constructor(url: String, extras: WebAssetSpec?) {
		this.url = url
		var spec: WebAssetSpec = UiSpec()
		if (extras != null) spec += extras
		this.spec = spec
		this.html = spec + UiTemplate.BASE
	}

	// --

	@Suppress("NOTHING_TO_INLINE")
	protected inline fun UiSpec() = UiSpec(this::class.qualifiedName)

	companion object {

		@Suppress("NOTHING_TO_INLINE")
		inline fun fqn(uiFqn: String?) = UiSpec(uiFqn)

		@Suppress("NOTHING_TO_INLINE")
		inline fun title(title: String?) = HtmlTitleSpec(title)
	}
}
