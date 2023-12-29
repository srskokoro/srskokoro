package build.base

import build.plugins.test.buildProject
import build.plugins.test.io.TestTemp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class test__plugin {

	@Test fun `The 'root' plugin is applied to root projects but not to children`() {
		val parent = buildProject(TestTemp.from(this, "0"))
		val child = buildProject(TestTemp.from(this, "0/0")) { withParent(parent) }

		assertTrue(parent.run {
			apply<_plugin>()
			plugins.hasPlugin(build.root._plugin::class)
		})

		assertFalse(child.run {
			apply<_plugin>()
			plugins.hasPlugin(build.root._plugin::class)
		})
	}

	@Test fun `All known convention plugins delegate to the 'base' plugin`() {
		assertTrue { hasBasePlugin<build.kt.jvm._plugin>() }
		assertTrue { hasBasePlugin<build.kt.jvm.lib._plugin>() }

		assertTrue { hasBasePlugin<build.kt.mpp._plugin>() }
		assertTrue { hasBasePlugin<build.kt.mpp.lib._plugin>() }

		assertTrue { hasBasePlugin<build.support.kt.jvm._plugin>() }
		assertTrue { hasBasePlugin<build.support.kt.mpp._plugin>() }
	}

	private inline fun <reified P : Plugin<Project>> hasBasePlugin() = hasBasePlugin(P::class.java)

	private fun hasBasePlugin(plugin: Class<out Plugin<Project>>): Boolean {
		return buildProject(TestTemp.from(this, plugin.name)).run {
			apply(fun(x) { x.plugin(plugin) })
			plugins.hasPlugin(_plugin::class)
		}
	}
}
