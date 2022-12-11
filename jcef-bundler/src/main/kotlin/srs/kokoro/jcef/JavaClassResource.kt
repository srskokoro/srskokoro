package srs.kokoro.jcef

import org.gradle.api.resources.MissingResourceException
import org.gradle.api.resources.ReadableResource
import java.io.InputStream
import java.net.URI
import java.net.URL

internal data class JavaClassResource(
	val ref: Class<*>,
	val name: String,
) : ReadableResource {
	@Suppress("MemberVisibilityCanBePrivate")
	val url: URL
		get() = ref.getResource(name) ?: errorNotFound()

	override fun getURI(): URI = url.toURI()

	override fun getDisplayName(): String = url.toString()

	override fun getBaseName(): String = name

	override fun read(): InputStream = ref.getResourceAsStream(name) ?: errorNotFound()

	private fun errorNotFound(): Nothing = throw MissingResourceException(
		"Expected classpath resource not found:\n$this"
	)

	override fun toString() = "[$ref] $name"
}
