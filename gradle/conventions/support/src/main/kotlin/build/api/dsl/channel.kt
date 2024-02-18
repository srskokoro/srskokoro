package build.api.dsl

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.attributes.Attribute

private val CHANNEL_ATTRIBUTE = Attribute.of("build.api.artifacts.channel", String::class.java)

/**
 * Creates a pair of configurations that acts as a "channel", useful for quickly
 * sharing outputs between projects.
 */
fun ConfigurationContainer.channel(name: String) {
	channelIncoming(name)
	channelOutgoing(name)
}

/**
 * @see channel
 */
fun ConfigurationContainer.channelIncoming(name: String): Configuration {
	val c = create(name)
	c.isCanBeConsumed = false // It's a resolvable configuration (and not consumable)
	setUpChannelConfiguration(c, name)
	return c
}

/**
 * @see channel
 */
fun ConfigurationContainer.channelOutgoing(name: String): Configuration {
	val c = create("${name}Elements")
	c.isCanBeResolved = false // It's a consumable configuration (and not resolvable)
	setUpChannelConfiguration(c, name)
	return c
}

private fun setUpChannelConfiguration(c: Configuration, channel: String) {
	c.isVisible = false
	c.attributes.attribute(CHANNEL_ATTRIBUTE, channel)
}
