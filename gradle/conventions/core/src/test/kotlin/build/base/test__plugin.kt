package build.base

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isTrue
import build.plugins.test.buildProject
import build.plugins.test.io.TestTemp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import kotlin.test.Test

class test__plugin {

	@Test fun `All known convention plugins delegate to the 'base' plugin`(): Unit = assertAll {
		assertTrue { hasBasePlugin<build.kt.jvm._plugin>() }
		assertTrue { hasBasePlugin<build.kt.jvm.lib._plugin>() }
		assertTrue { hasBasePlugin<build.kt.jvm.multipurpose._plugin>() }

		assertTrue { hasBasePlugin<build.kt.mpp._plugin>() }
		assertTrue { hasBasePlugin<build.kt.mpp.lib._plugin>() }
		assertTrue { hasBasePlugin<build.kt.mpp.multipurpose._plugin>() }

		assertTrue { hasBasePlugin<build.plugins._plugin>() }
		assertTrue { hasBasePlugin<build.plugins.support._plugin>() }
	}

	private inline fun assertTrue(block: () -> Boolean) = assertThat(block()).isTrue()

	private inline fun <reified P : Plugin<Project>> hasBasePlugin() = hasBasePlugin(P::class.java)

	private fun hasBasePlugin(plugin: Class<out Plugin<Project>>): Boolean {
		return buildProject(TestTemp.from(this, plugin.name)).run {
			apply(fun(x) { x.plugin(plugin) })
			plugins.hasPlugin(_plugin::class)
		}
	}
}