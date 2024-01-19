package build.structure

import build.plugins.test.io.TestTemp
import build.structure.ProjectStructure.*
import build.support.from
import build.support.io.buildDir
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.io.File
import java.util.LinkedList
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

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

	@Test fun `BUILD_HOISTED findProjects`() {
		assertProjectStructure(BUILD_HOISTED, BUILD_HOISTED_expected)
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

		val actual = output.mapTo(ArrayList()) { it.getProjectId(root) }.apply { sort() }
		assertContentEquals(expectedProjectIds, actual)

		// Also assert that the resulting project directories are correct
		val rootPath_n = root.path.length + 1
		assertEquals(output.size, output.count { it.getProjectDir(root).path.from(rootPath_n) in SETUP })
	}
}

// --

private fun entriesFrom(vararg entries: String): LinkedHashSet<String> {
	return entries.mapTo(LinkedHashSet()) { it.replace('/', File.separatorChar) }
}

private fun expectedList(vararg expected: String): List<String> {
	return expected.asList()//.sorted()
}


private val SETUP = entriesFrom(
	"alice",
	"alice/@a",
	"alice/b",
	"alice/c",
	"alice/d",
	"alice/e",
	"alice/e/hi",
	"alice/e/hi/hello",
	"alice/e/hi/hello/world",
	"alice/e/hi/hello/world/foo",
	"alice/e/hi/hello/world/foo/bar",
	"alice/e/hi/hello/world/foo/bar/baz",
	"alice/e/hi/hello/world/foo/bar/baz/x",
	"alice/e/hi/hello/world/foo/bar/baz/x/y",
	"alice/e/hi/hello/world/foo/bar/baz/x/y/z",
	"bob",
	"bob/[foo]a",
	"bob/b",
	"bob/b/hi",
	"bob/b/hello",
	"bob/b/hello/!right",
	"bob/b/hello/[x]down",
	"bob/b/hello/left",
	"bob/b/hello/left/blue",
	"bob/b/hello/left/blue/[x]d",
	"bob/b/hello/left/green",
	"bob/b/hello/left/red",
	"bob/b/hello/up",
	"bob/b/world",
	"bob/b/world/[0]foo",
	"bob/b/world/[1]bar",
	"bob/b/world/[1]bar/beep",
	"bob/b/world/[1]bar/bop",
	"bob/b/world/baz",
	"bob/b/world/baz/oof",
	"bob/b/world/baz/oof/victor",
	"bob/b/world/baz/oof/victor/vanna",
	"bob/ignored/bar",
	"bob/ignored/baz",
	"bob/ignored/foo",
	"bob/#c",
	"carol",
	"dan",
	"erin",
	"erin/z",
	"erin/z/blue",
	"erin/z/green",
	"erin/z/green/a",
	"erin/z/green/b",
	"erin/z/green/c",
	"erin/z/green/c/foo",
	"erin/z/green/c/foobar",
	"erin/z/red",
	"eve",
	"faythe",
	"frank",
	"frank/x",
	"frank/y",
	"grace",
	"heidi",
	"heidi/foo",
	"heidi/foo/bar",
	"ivan",
	"ivan/~x",
	"ivan/~x/arthur",
	"ivan/~x/arthur/\$carole",
	"ivan/~x/arthur/paul",
	"ivan/~x/merlin",
	"ivan/y",
	"ivan/y/-arthur",
	"ivan/y/[martha]bertha",
	"ivan/y/[martha]bertha/peggy",
	"ivan/y/[martha]bertha/peggy/pat",
	"judy",
)

private val NO_BUILD_SCRIPTS = entriesFrom(
	"bob/b/hi",
	"bob/ignored",
	"dan",
)

private val INCLUDES_expected = expectedList(
	":alice",
	":alice:a",
	":alice:b",
	":alice:c",
	":alice:d",
	":bob",
	":bob:a",
	":bob:b",
	":bob:b:world",
	":bob:b:world:baz",
	":bob:b:world:foo",
	":bob:c",
	":carol",
	":erin",
	":erin:z",
	":erin:z:green",
	":erin:z:green:a",
	":erin:z:green:b",
	":erin:z:red",
	":eve",
	":faythe",
	":frank",
	":frank:y",
	":grace",
	":heidi",
	":ivan",
	":ivan:y",
	":ivan:y:arthur",
	":ivan:y:bertha",
)


private val WITH_BUILD_HOISTED_MARKS = entriesFrom(
	"bob/b/world/baz/oof",
	"erin/z/green/c",
	"erin/z/blue",
	"frank/x",
	"heidi/foo",
	"heidi/foo/bar",
	"judy",
)

private val BUILD_HOISTED_expected = expectedList(
	":bob[.]b[.]world[.]baz[.]oof",
	":erin[.]z[.]blue",
	":erin[.]z[.]green[.]c",
	":erin[.]z[.]green[.]c:foo",
	":erin[.]z[.]green[.]c:foobar",
	":frank[.]x",
	":heidi[.]foo",
	":heidi[.]foo[.]bar",
	":judy",
)


private val WITH_BUILD_INCLUSIVE_MARKS = entriesFrom(
	"alice/e",
	"alice/e/hi/hello/world/foo/bar/baz",
	"bob/b/hello/left/blue",
	"bob/b/world/[1]bar",
	"ivan/~x",
	"ivan/y/[martha]bertha/peggy",
	"ivan/y/[martha]bertha/peggy/pat",
)

private val BUILD_INCLUSIVES_expected = expectedList(
	":alice[.]e",
	":alice[.]e:hi",
	":alice[.]e[.]hi[.]hello[.]world[.]foo[.]bar[.]baz",
	":alice[.]e[.]hi[.]hello[.]world[.]foo[.]bar[.]baz:x",
	":alice[.]e[.]hi[.]hello[.]world[.]foo[.]bar[.]baz:x:y",
	":alice[.]e[.]hi[.]hello[.]world[.]foo[.]bar[.]baz:x:y:z",
	":bob[.]b[.]hello[.]left[.]blue",
	":bob[.]b[.]hello[.]left[.]blue:d",
	":bob[.]b[.]world[.]bar",
	":bob[.]b[.]world[.]bar:beep",
	":bob[.]b[.]world[.]bar:bop",
	":ivan[.]x",
	":ivan[.]x:arthur",
	":ivan[.]x:arthur:carole",
	":ivan[.]x:arthur:paul",
	":ivan[.]x:merlin",
	":ivan[.]y[.]bertha[.]peggy",
	":ivan[.]y[.]bertha[.]peggy[.]pat",
)


private val WITH_BUILD_PLUGIN_MARKS = entriesFrom(
	"alice/e/hi/hello",
	"bob/b/hello",
	"bob/b/hello/[x]down",
	"bob/b/world/baz/oof/victor",
	"bob/b/world/baz/oof/victor/vanna",
)

private val BUILD_PLUGINS_expected = expectedList(
	":alice[.]e[.]hi[.]hello",
	":alice[.]e[.]hi[.]hello:world",
	":alice[.]e[.]hi[.]hello:world:foo",
	":alice[.]e[.]hi[.]hello:world:foo:bar",
	":bob[.]b[.]hello",
	":bob[.]b[.]hello:left",
	":bob[.]b[.]hello:left:green",
	":bob[.]b[.]hello:left:red",
	":bob[.]b[.]hello:right",
	":bob[.]b[.]hello:up",
	":bob[.]b[.]hello[.]down",
	":bob[.]b[.]world[.]baz[.]oof[.]victor",
	":bob[.]b[.]world[.]baz[.]oof[.]victor[.]vanna",
)


@Suppress("SameParameterValue")
private fun setUpProjectTree(rootDir: File) {
	val setup: LinkedHashSet<String> = SETUP
	val noBuildScripts: LinkedHashSet<String> = NO_BUILD_SCRIPTS

	val withBuildHoistedMarks: LinkedHashSet<String> = WITH_BUILD_HOISTED_MARKS
	val withBuildInclusiveMarks: LinkedHashSet<String> = WITH_BUILD_INCLUSIVE_MARKS
	val withBuildPluginMarks: LinkedHashSet<String> = WITH_BUILD_PLUGIN_MARKS

	buildDir(rootDir) {
		for (path in setup) {
			dir(path) {
				if (path !in noBuildScripts) {
					file("build.gradle.kts").createNewFile()
					if (path in withBuildHoistedMarks)
						file("build-hoisted.mark").createNewFile()
					if (path in withBuildInclusiveMarks)
						file("build-inclusive.mark").createNewFile()
					if (path in withBuildPluginMarks)
						file("build-plugin.mark").createNewFile()
				}
			}
		}
	}
}
