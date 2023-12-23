package build.dependencies

import build.api.support.io.UnsafeCharArrayWriter
import build.api.testing.io.TestTempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class test_DepsCoder {

	@Test fun `encodeFully decodeFully`() {
		val settingDir = TestTempDir.from(this)
		val buffer = UnsafeCharArrayWriter()

		val input = BaseDependencySettings().apply {
			props["46"] = "Pd"
			props["X"] = ","
			props["P"] = ",,"
			props["M"] = ",,,"
			props["I"] = ",,,,\n"

			props["foo,bar,%"] = "%C%E%n%r"
			props["hello\nworld"] = "Foo,Bar,%,\r"

			plugins[PluginId.of_unsafe("alice.bob")] = "1.0#p"
			modules[ModuleId.of_unsafe("alice:bob")] = "1.0#m"

			prioritizeForLoad(File(settingDir, "relative"))
		}

		DepsEncoder(input, buffer, settingDir).encodeFully()
		val data = buffer.toString()

		assertEquals(
			DepsCoder_version + '\n' + """
			X,46,Pd
			X,X,%C
			X,P,%C%C
			X,M,%C%C%C
			X,I,%C%C%C%C%n
			X,foo%Cbar%C%E,%EC%EE%En%Er
			X,hello%nworld,Foo%CBar%C%E%C%r
			P,alice.bob,1.0#p
			M,alice:bob,1.0#m
			I,relative
			""".trimIndent() + '\n',
			data,
		)

		val output = BaseDependencySettings()
		DepsDecoder(output, data, settingDir).decodeFully()

		assertEquals(
			input.props,
			output.props,
		)
		assertEquals(
			input.plugins,
			output.plugins,
		)
		assertEquals(
			input.modules,
			output.modules,
		)
		assertContentEquals(
			input.includedBuildsDeque,
			output.includedBuildsDeque,
		)
	}
}
