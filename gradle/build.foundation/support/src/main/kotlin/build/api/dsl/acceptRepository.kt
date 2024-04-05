package build.api.dsl

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.attributes.Attribute

private const val ACCEPTED = "accepted"

private fun acceptRepositoryAttr(repositoryName: String): Attribute<String> =
	Attribute.of("build.api.artifacts.repositories:$repositoryName", String::class.java)

// --

fun Configuration.acceptRepository(repositoryName: String) {
	attributes.attribute(acceptRepositoryAttr(repositoryName), ACCEPTED)
}

/**
 * @see onlyIfAcceptedAs
 */
fun ArtifactRepository.onlyIfAccepted() {
	val name = checkNotNull(this.name) { "Must first set a name." }
	content { onlyForAttribute(acceptRepositoryAttr(name), ACCEPTED) }
}

/**
 * Similar to [onlyIfAccepted]`()` but also sets the [name][ArtifactRepository.getName]
 * for this [ArtifactRepository]. If [name][ArtifactRepository.getName] is
 * already set to a nonnull value, then the `name` given here must be the same
 * (or an exception is thrown).
 */
fun ArtifactRepository.onlyIfAcceptedAs(name: String) {
	val currentName: String? = this.name
	if (currentName != name) {
		check(currentName == null) { "Name already set." }
		this.name = name
	}
	return onlyIfAccepted()
}
