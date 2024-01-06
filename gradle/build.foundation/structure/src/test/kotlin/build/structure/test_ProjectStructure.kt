package build.structure

import build.plugins.test.io.TestTemp
import build.structure.ProjectStructure.*
import build.support.io.buildDir
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.io.File
import java.util.LinkedList
import kotlin.test.Test
import kotlin.test.assertContentEquals

class test_ProjectStructure {

	companion object {
		private val root = TestTemp.from(this)

		@JvmStatic
		@BeforeAll fun setUp(): Unit = setUpProjectTree(root)

		@JvmStatic
		@AfterAll fun tearDown(): Unit = root.deleteRecursively().let {}
	}

	@Test fun `INCLUDES findProjects`() {
		assertProjectStructure(INCLUDES, INCLUDES_expected)
	}

	@Test fun `BUILD_INCLUSIVES findProjects`() {
		assertProjectStructure(BUILD_INCLUSIVES, BUILD_INCLUSIVES_expected)
	}

	@Test fun `BUILD_PLUGINS findProjects`() {
		assertProjectStructure(BUILD_PLUGINS, BUILD_PLUGINS_expected)
	}

	private fun assertProjectStructure(structure: ProjectStructure, expectedProjectIds: List<String>) {
		val output = LinkedList<ProjectEntry>()
		structure.findProjects(root, output)

		val actual = output.mapTo(ArrayList()) { it.projectId }.apply { sort() }
		assertContentEquals(expectedProjectIds, actual)
	}
}

// --

private val SETUP_WITHOUT_BUILD_SCRIPTS = setOf(
	"bob/b/hi/",
	"bob/ignored/",
	"erin/",
)

private val SETUP_WITH_BUILD_SCRIPTS = listOf(
	"alice/",
	"alice/@a/",
	"alice/b/",
	"alice/c/",
	"alice/d/",
	"alice/e/",
	"alice/e/hi/",
	"alice/e/hi/hello/",
	"alice/e/hi/hello/world/",
	"alice/e$D_BUILD_INCLUSIVE/",
	"alice/e$D_BUILD_INCLUSIVE/hi/",
	"alice/e$D_BUILD_INCLUSIVE/hi/hello$D_BUILD_PLUGIN/",
	"alice/e$D_BUILD_INCLUSIVE/hi/hello$D_BUILD_PLUGIN/world/",
	"alice/e$D_BUILD_INCLUSIVE/hi/hello$D_BUILD_PLUGIN/world/foo/",
	"alice/e$D_BUILD_INCLUSIVE/hi/hello$D_BUILD_PLUGIN/world/foo/bar/",
	"alice/e$D_BUILD_INCLUSIVE/hi/hello$D_BUILD_PLUGIN/world/foo/bar/baz$D_BUILD_INCLUSIVE/",
	"alice/e$D_BUILD_INCLUSIVE/hi/hello$D_BUILD_PLUGIN/world/foo/bar/baz$D_BUILD_INCLUSIVE/x/",
	"alice/e$D_BUILD_INCLUSIVE/hi/hello$D_BUILD_PLUGIN/world/foo/bar/baz$D_BUILD_INCLUSIVE/x/y/",
	"alice/e$D_BUILD_INCLUSIVE/hi/hello$D_BUILD_PLUGIN/world/foo/bar/baz$D_BUILD_INCLUSIVE/x/y/z/",
	"bob/",
	"bob/[foo]a/",
	"bob/b/",
	"bob/b/hello$D_BUILD_PLUGIN/",
	"bob/b/hello$D_BUILD_PLUGIN/left/",
	"bob/b/hello$D_BUILD_PLUGIN/left/dan$D_BUILD_INCLUSIVE/",
	"bob/b/hello$D_BUILD_PLUGIN/left/dan$D_BUILD_INCLUSIVE/[x]d/",
	"bob/b/hello$D_BUILD_PLUGIN/left/dave/",
	"bob/b/hello$D_BUILD_PLUGIN/left/david/",
	"bob/b/hello$D_BUILD_PLUGIN/up/",
	"bob/b/hello$D_BUILD_PLUGIN/!right/",
	"bob/b/hello$D_BUILD_PLUGIN/down/",
	"bob/b/hello$D_BUILD_PLUGIN/[x]down$D_BUILD_PLUGIN/",
	"bob/b/world/",
	"bob/b/world/[0]foo/",
	"bob/b/world/[1]bar$D_BUILD_INCLUSIVE/",
	"bob/b/world/[1]bar$D_BUILD_INCLUSIVE/beep/",
	"bob/b/world/[1]bar$D_BUILD_INCLUSIVE/bop/",
	"bob/b/world/baz/",
	"bob/b/world/baz/oof/",
	"bob/b/world/baz/oof/victor$D_BUILD_PLUGIN/",
	"bob/b/world/baz/oof/victor$D_BUILD_PLUGIN/vanna$D_BUILD_PLUGIN/",
	"bob/b/world/baz/oof$D_BUILD_PLUGIN/",
	"bob/ignored/bar/",
	"bob/ignored/baz$D_BUILD_PLUGIN/",
	"bob/#c/",
	"carol/",
	"dan/",
	"frank/",
	"frank/x/",
	"frank/y/",
	"frank/~y$D_BUILD_INCLUSIVE/",
	"frank/~y$D_BUILD_INCLUSIVE/arthur/",
	"frank/~y$D_BUILD_INCLUSIVE/arthur/paul/",
	"frank/~y$D_BUILD_INCLUSIVE/arthur/\$carole/",
	"frank/~y$D_BUILD_INCLUSIVE/merlin/",
	"frank/z/",
	"frank/z/-arthur/",
	"frank/z/[martha]bertha/",
	"frank/z/[martha]bertha/peggy$D_BUILD_INCLUSIVE/",
	"frank/z/[martha]bertha/peggy$D_BUILD_INCLUSIVE/pat$D_BUILD_INCLUSIVE/",
)

private val INCLUDES_expected = arrayOf(
	":alice",
	":alice:a",
	":alice:b",
	":alice:c",
	":alice:d",
	":alice:e",
	":alice:e:hi",
	":alice:e:hi:hello",
	":alice:e:hi:hello:world",
	":bob",
	":bob:a",
	":bob:b",
	":bob:b:world",
	":bob:b:world:baz",
	":bob:b:world:baz:oof",
	":bob:b:world:foo",
	":bob:c",
	":carol",
	":dan",
	":frank",
	":frank:x",
	":frank:y",
	":frank:z",
	":frank:z:arthur",
	":frank:z:bertha",
).asList()//.sorted()

private val BUILD_INCLUSIVES_expected = arrayOf(
	":alice[.]e",
	":alice[.]e:hi",
	":alice[.]e[.]hi[.]hello[.]world[.]foo[.]bar[.]baz",
	":alice[.]e[.]hi[.]hello[.]world[.]foo[.]bar[.]baz:x",
	":alice[.]e[.]hi[.]hello[.]world[.]foo[.]bar[.]baz:x:y",
	":alice[.]e[.]hi[.]hello[.]world[.]foo[.]bar[.]baz:x:y:z",
	":bob[.]b[.]hello[.]left[.]dan",
	":bob[.]b[.]hello[.]left[.]dan:d",
	":bob[.]b[.]world[.]bar",
	":bob[.]b[.]world[.]bar:beep",
	":bob[.]b[.]world[.]bar:bop",
	":frank[.]y",
	":frank[.]y:arthur",
	":frank[.]y:arthur:carole",
	":frank[.]y:arthur:paul",
	":frank[.]y:merlin",
	":frank[.]z[.]bertha[.]peggy",
	":frank[.]z[.]bertha[.]peggy[.]pat",
).asList()//.sorted()

private val BUILD_PLUGINS_expected = arrayOf(
	":alice[.]e[.]hi[.]hello",
	":alice[.]e[.]hi[.]hello:world",
	":alice[.]e[.]hi[.]hello:world:foo",
	":alice[.]e[.]hi[.]hello:world:foo:bar",
	":bob[.]b[.]hello",
	":bob[.]b[.]hello:down",
	":bob[.]b[.]hello:left",
	":bob[.]b[.]hello:left:dave",
	":bob[.]b[.]hello:left:david",
	":bob[.]b[.]hello:right",
	":bob[.]b[.]hello:up",
	":bob[.]b[.]hello[.]down",
	":bob[.]b[.]world[.]baz[.]oof",
	":bob[.]b[.]world[.]baz[.]oof[.]victor",
	":bob[.]b[.]world[.]baz[.]oof[.]victor[.]vanna",
).asList()//.sorted()

@Suppress("SameParameterValue", "NAME_SHADOWING")
private fun setUpProjectTree(rootDir: File) {
	val withBuildScripts = SETUP_WITH_BUILD_SCRIPTS
	val withoutBuildScript = SETUP_WITHOUT_BUILD_SCRIPTS

	buildDir(rootDir) {
		for (path in withoutBuildScript) dir(path)
		for (path in withBuildScripts) dir(path) {
			file("build.gradle.kts").createNewFile()
		}
	}
}
