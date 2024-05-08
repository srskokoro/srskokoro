package kokoro.app.ui.engine.web

import kotlin.jvm.JvmField

/**
 * @see WebAssetTemplate.plus
 * @see WebAssetSpec.plus
 */
data class WebAsset(
	@JvmField val template: WebAssetTemplate,
	@JvmField val spec: WebAssetSpec,
) : WebResource {

	constructor() : this(WebAssetTemplate.EMPTY, WebAssetSpec.EMPTY)
	constructor(template: WebAssetTemplate) : this(template, WebAssetSpec.EMPTY)

	// --

	override suspend fun apply(request: WebRequest) = template.apply(request, spec)
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun WebAssetTemplate.plus(spec: WebAssetSpec) = WebAsset(this, spec)

@Suppress("NOTHING_TO_INLINE")
inline operator fun WebAssetSpec.plus(template: WebAssetTemplate) = WebAsset(template, this)
