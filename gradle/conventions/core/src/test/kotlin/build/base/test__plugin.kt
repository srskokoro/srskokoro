package build.base

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isTrue
import build.plugins.test.buildProject
import build.plugins.test.io.TestTemp
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestScope
import org.gradle.api.Plugin
import org.gradle.api.Project

class test__plugin : FreeSpec({
	"All known convention plugins delegate to the 'base' plugin" {
		assertAll {
			assertTrue { hasBasePlugin<build.kt.jvm._plugin>() }
			assertTrue { hasBasePlugin<build.kt.jvm.lib._plugin>() }
			assertTrue { hasBasePlugin<build.kt.jvm.inclusive._plugin>() }

			assertTrue { hasBasePlugin<build.kt.mpp._plugin>() }
			assertTrue { hasBasePlugin<build.kt.mpp.lib._plugin>() }
			assertTrue { hasBasePlugin<build.kt.mpp.inclusive._plugin>() }

			assertTrue { hasBasePlugin<build.plugins._plugin>() }
			assertTrue { hasBasePlugin<build.plugins.support._plugin>() }
		}
	}
})

private inline fun assertTrue(block: () -> Boolean) = assertThat(block()).isTrue()

private inline fun <reified P : Plugin<Project>> TestScope.hasBasePlugin() = hasBasePlugin(P::class.java)

private fun TestScope.hasBasePlugin(plugin: Class<out Plugin<Project>>): Boolean {
	return buildProject(TestTemp.from(this, plugin.name)).run {
		apply(fun(x) { x.plugin(plugin) })
		plugins.hasPlugin(_plugin::class.java.packageName)
	}
}
