package kokoro.app.ui.engine.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.kotest.core.spec.style.FreeSpec
import okio.ByteString.Companion.encode
import okio.ByteString.Companion.encodeUtf8

@Suppress("SpellCheckingInspection")
private const val FEFF = "\uFEFF"

class test_Bom : FreeSpec({
	"BOM sequence for UTF-8 is as expected" {
		assertThat(Bom.UTF_8).isEqualTo(FEFF.encodeUtf8())
	}
	"BOM sequence for UTF-16BE is as expected" {
		assertThat(Bom.UTF_16BE).isEqualTo(FEFF.encode(Charsets.UTF_16BE))
	}
	"BOM sequence for UTF-16LE is as expected" {
		assertThat(Bom.UTF_16LE).isEqualTo(FEFF.encode(Charsets.UTF_16LE))
	}
	"BOM sequence for UTF-32BE is as expected" {
		assertThat(Bom.UTF_32BE).isEqualTo(FEFF.encode(Charsets.UTF_32BE))
	}
	"BOM sequence for UTF-32LE is as expected" {
		assertThat(Bom.UTF_32LE).isEqualTo(FEFF.encode(Charsets.UTF_32LE))
	}
})
