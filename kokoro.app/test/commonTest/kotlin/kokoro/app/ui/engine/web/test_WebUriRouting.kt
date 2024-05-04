package kokoro.app.ui.engine.web

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotSameInstanceAs
import assertk.assertions.isNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isSuccess
import assertk.assertions.isTrue
import build.test.assertResult
import build.test.assertWith
import io.kotest.core.spec.style.FreeSpec

class test_WebUriRouting : FreeSpec({

	"Property `uri` doesn't end with '*' on build" {
		assertAll {
			assertWith {
				WebUriRouting { route("foo*") }.entries.single().uri
			}.isEqualTo("foo")

			assertWith {
				WebUriRouting.Builder()
					.route("https://example.com/*")
					.build().entries.single().uri
			}.endsWith("/")

			assertWith {
				WebUriRouting {
					route("https://example.com/*", false)
				}.entries.single().uri
			}.endsWith("/")

			assertWith {
				WebUriRouting {
					route("https://example.com/*", true)
				}.entries.single().uri
			}.endsWith("/")
		}
	}

	"Property `isUriPrefix` is as expected" {
		assertAll {
			assertWith {
				WebUriRouting {
					route("https://example.com/")
				}.entries.single().isUriPrefix
			}.isFalse()

			assertWith {
				WebUriRouting {
					route("https://example.com/", false)
				}.entries.single().isUriPrefix
			}.isFalse()

			assertWith {
				WebUriRouting {
					route("https://example.com/", true)
				}.entries.single().isUriPrefix
			}.isTrue()

			assertWith {
				WebUriRouting {
					route("https://example.com/*", false)
				}.entries.single().isUriPrefix
			}.isTrue()
		}
	}

	"Fails on duplicate entries" {
		assertAll {
			assertResult {
				WebUriRouting.Builder()
					.route("foo")
					.route("foo")
			}.isSuccess().transform {
				it.runCatching { build() }
			}.isFailure().isInstanceOf<IllegalStateException>()

			assertResult {
				WebUriRouting {
					route("bar")
					route("bar")
				}
			}.isFailure().isInstanceOf<IllegalStateException>()
		}
	}

	"Built entries are immutable" {
		assertResult {
			WebUriRouting { route("x://hello/world") }
				.entries.let { it as MutableList }
				.clear()
		}.isFailure().isInstanceOf<UnsupportedOperationException>()
	}

	"Builder via already built entries works as expected" {
		assertAll {

			assertThat(
				WebUriRouting { route("foo") }
					.builder().route("bar").sort()
					.entries.map { it.uri to it.isUriPrefix }
			).isEqualTo(
				WebUriRouting {
					route("foo")
					route("bar")
				}.entries.map { it.uri to it.isUriPrefix }
			)

			assertThat(
				WebUriRouting { route("foo") }
					.copy { route("bar") }
					.entries.map { it.uri to it.isUriPrefix }
			).isEqualTo(
				WebUriRouting {
					route("foo")
					route("bar")
				}.entries.map { it.uri to it.isUriPrefix }
			)

		}
	}

	"Sorting is as expected" {
		assertAll {

			assertThat(
				WebUriRouting {
					route("x://foo/")
					route("x://foo")
					route("x://bar/*")
					route("x://bar")
					route("x://foo/*")
					route("x://baz-*")
				}.entries.map { it.uri to it.isUriPrefix }
			).isEqualTo(
				WebUriRouting.Builder().apply {
					route("x://bar")
					route("x://foo")
					route("x://foo/")
					route("x://bar/*")
					route("x://baz-*")
					route("x://foo/*")
				}.entries.map { it.uri to it.isUriPrefix }
			)

			assertThat(
				WebUriRouting {
					route("x://foo/")
					route("x://foo")
					route("x://bar/*")
					route("x://bar")
					route("x://foo/*")
					route("x://baz-*")
				}.entries.map { it.uri to it.isUriPrefix }
			).isEqualTo(
				WebUriRouting.Builder().apply {
					route("x://foo/")
					route("x://foo")
					route("x://bar/*")
					route("x://bar")
					route("x://foo/*")
					route("x://baz-*")
					sort()
				}.entries.map { it.uri to it.isUriPrefix }
			)
		}
	}

	"Count is as expected" {
		assertAll {
			WebUriRouting {
				route("x://foo/")
				route("x://bar/*")
				route("x://bar")
				route("x://foo/*")
				route("x://baz-*")
			}.apply {
				assertThat(nonPrefixUriCount).isEqualTo(2)
				assertThat(entries.size - nonPrefixUriCount).isEqualTo(3)
			}
		}
	}

	"Resolves as expected" {
		assertAll {
			val sample = WebRequestHandler.EMPTY
			val foo = WebRequestHandler()
			val bar = WebRequestHandler()
			val baz = WebRequestHandler()
			val dOne = WebRequestHandler()
			val dOneStar = WebRequestHandler()
			val cSlashStar = WebRequestHandler()
			val loneStar = WebRequestHandler()

			WebUriRouting {
				route("https://example.com", sample)
				route("sample", sample)
				route("foo", foo)
				route("bar", bar)
				route("baz", baz)
				route("d://one/", dOne)
				route("d://one/*", dOneStar)
				route("c:///*", cSlashStar)
				route("lone*", loneStar)
			}.apply {
				assertThat(resolve(WebUri("https://example.com"))).isSameInstanceAs(sample)
				assertThat(resolve(WebUri("sample"))).isSameInstanceAs(sample)

				assertThat(resolve(WebUri("foo"))).apply {
					isSameInstanceAs(foo)
					isNotSameInstanceAs(bar)
				}
				assertThat(resolve(WebUri("bar"))).isSameInstanceAs(bar)
				assertThat(resolve(WebUri("baz"))).isSameInstanceAs(baz)

				assertThat(resolve(WebUri("d://one/"))).isSameInstanceAs(dOne)
				assertThat(resolve(WebUri("d://one/world"))).isSameInstanceAs(dOneStar)

				assertThat(resolve(WebUri("c:///"))).isSameInstanceAs(cSlashStar)
				assertThat(resolve(WebUri("c:///foo/bar/baz"))).isSameInstanceAs(cSlashStar)

				assertThat(resolve(WebUri("lone"))).isSameInstanceAs(loneStar)
				assertThat(resolve(WebUri("lone-one"))).isSameInstanceAs(loneStar)

				assertThat(resolve(WebUri("gone"))).isNull()
				assertThat(resolve(WebUri("x"))).isNull()
				assertThat(resolve(WebUri(""))).isNull()
			}

			WebUriRouting {
				route("foo", foo)
			}.apply {
				assertThat(resolve(WebUri("foo"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("missing"))).isNull()
				assertThat(resolve(WebUri(""))).isNull()
			}

			WebUriRouting {
				route("lone*", loneStar)
			}.apply {
				assertThat(resolve(WebUri("lone"))).isSameInstanceAs(loneStar)
				assertThat(resolve(WebUri("lonely-one"))).isSameInstanceAs(loneStar)
				assertThat(resolve(WebUri("missing"))).isNull()
				assertThat(resolve(WebUri(""))).isNull()
			}

			WebUriRouting().apply {
				assertThat(resolve(WebUri("missing"))).isNull()
				assertThat(resolve(WebUri(""))).isNull()
			}

			WebUriRouting.EMPTY.apply {
				assertThat(resolve(WebUri("missing"))).isNull()
				assertThat(resolve(WebUri(""))).isNull()
			}
		}
	}

	"Must not be the same instances" {
		assertAll {
			assertThat(WebUriRouting.EMPTY)
				.isNotSameInstanceAs(WebUriRouting())
			assertThat(WebUriRouting())
				.isNotSameInstanceAs(WebUriRouting())
		}
	}
})
