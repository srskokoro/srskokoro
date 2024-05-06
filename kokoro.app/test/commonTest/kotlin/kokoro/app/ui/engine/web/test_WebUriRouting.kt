package kokoro.app.ui.engine.web

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.hasHashCode
import assertk.assertions.hashCodeFun
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
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
					.builder().route("bar")
					.sort().entries
			).isEqualTo(
				WebUriRouting {
					route("foo")
					route("bar")
				}.entries
			)

			assertThat(
				WebUriRouting { route("foo") }
					.copy { route("bar") }
					.entries
			).isEqualTo(
				WebUriRouting {
					route("foo")
					route("bar")
				}.entries
			)

			assertThat(
				WebUriRouting {
					route("-*")
					route("foo")
					route("bar")
				}.builder().route("more")
					.build().entries
			).isEqualTo(
				WebUriRouting {
					route("-*")
					route("foo")
					route("bar")
					route("more")
				}.entries
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
				}.entries
			).isEqualTo(
				WebUriRouting.Builder().apply {
					route("x://bar")
					route("x://foo")
					route("x://foo/")
					route("x://bar/*")
					route("x://baz-*")
					route("x://foo/*")
				}.entries
			)

			assertThat(
				WebUriRouting {
					route("x://foo/")
					route("x://foo")
					route("x://bar/*")
					route("x://bar")
					route("x://foo/*")
					route("x://baz-*")
				}.entries
			).isEqualTo(
				WebUriRouting.Builder().apply {
					route("x://foo/")
					route("x://foo")
					route("x://bar/*")
					route("x://bar")
					route("x://foo/*")
					route("x://baz-*")
					sort()
				}.entries
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

	"Must not be the same instances" {
		assertAll {
			assertThat(WebUriRouting.EMPTY)
				.isNotSameInstanceAs(WebUriRouting())
			assertThat(WebUriRouting())
				.isNotSameInstanceAs(WebUriRouting())
		}
	}

	"Same entries mean equal instances" {
		assertAll {
			fun assertEquals(a: WebUriRouting, b: WebUriRouting) {
				assertThat(a.entries).isEqualTo(b.entries)
				assertThat(a).isEqualTo(b)
				assertThat(a).hasHashCode(b.hashCode())
			}
			assertEquals(
				WebUriRouting(),
				WebUriRouting(),
			)
			assertEquals(
				WebUriRouting { route("x://foo/") },
				WebUriRouting { route("x://foo/") },
			)
			assertEquals(
				WebUriRouting { route("x://bar/*") },
				WebUriRouting { route("x://bar/*") },
			)
			assertEquals(
				WebUriRouting {
					route("x://foo/")
					route("x://bar/*")
				},
				WebUriRouting {
					route("x://bar/*")
					route("x://foo/")
				},
			)
			assertEquals(
				WebUriRouting {
					route("x://foo/*")
					route("x://bar/*")
				},
				WebUriRouting {
					route("x://bar/*")
					route("x://foo/*")
				},
			)
			assertEquals(
				WebUriRouting {
					route("x://foo")
					route("x://bar")
				},
				WebUriRouting {
					route("x://bar")
					route("x://foo")
				},
			)
			assertEquals(
				WebUriRouting {
					route("x://foo/")
					route("x://bar/*")
					route("x://baz")
				},
				WebUriRouting {
					route("x://baz")
					route("x://foo/")
					route("x://bar/*")
				},
			)
			assertEquals(
				WebUriRouting {
					route("x://foo-*")
					route("x://bar/")
					route("x://baz/*")
				},
				WebUriRouting {
					route("x://baz/*")
					route("x://foo-*")
					route("x://bar/")
				},
			)
		}
	}

	"Differing entries mean unequal instances" {
		assertAll {
			fun assertNotEquals(a: WebUriRouting, b: WebUriRouting) {
				assertThat(a.entries).isNotEqualTo(b.entries)
				assertThat(a).isNotEqualTo(b)
				assertThat(a).hashCodeFun().isNotEqualTo(b.hashCode())
			}
			assertNotEquals(
				WebUriRouting { route("x://foo/") },
				WebUriRouting(),
			)
			assertNotEquals(
				WebUriRouting(),
				WebUriRouting { route("x://foo/") },
			)
			assertNotEquals(
				WebUriRouting { route("x://foo/") },
				WebUriRouting { route("x://bar/") },
			)
			assertNotEquals(
				WebUriRouting { route("x://foo/"); route("x://bar/") },
				WebUriRouting { route("x://foo/"); route("x://bar/*") },
			)
			assertNotEquals(
				WebUriRouting { route("x://foo/"); route("x://bar/") },
				WebUriRouting { route("x://foo/*"); route("x://bar/*") },
			)
			assertNotEquals(
				WebUriRouting { route("x://foo/*"); route("x://bar/") },
				WebUriRouting { route("x://foo/*"); route("x://bar/*") },
			)
			assertNotEquals(
				WebUriRouting { route("x://foo/"); route("x://bar/*") },
				WebUriRouting { route("x://foo/"); route("x://bar/") },
			)
			assertNotEquals(
				WebUriRouting { route("x://foo/*"); route("x://bar/*") },
				WebUriRouting { route("x://foo/"); route("x://bar/") },
			)
			assertNotEquals(
				WebUriRouting { route("x://foo/*"); route("x://bar/*") },
				WebUriRouting { route("x://foo/"); route("x://bar/*") },
			)
		}
	}

	"Resolves as expected" {
		assertAll {
			val sample = WebRequestHandler.EMPTY
			val foo = WebRequestHandler()
			val bar = WebRequestHandler()
			val baz = WebRequestHandler()

			val dummies = object {
				private val emptyUri = WebUri("")
				private val dummyUri = WebUri("dummy")
				private val zDummyUri = WebUri("z-dummy")
				private val anotherDummyUri = WebUri("another-dummy")

				fun assertNulls(routing: WebUriRouting): Unit = with(routing) {
					assertThat(resolve(emptyUri)).isNull()
					assertThat(resolve(dummyUri)).isNull()
					assertThat(resolve(zDummyUri)).isNull()
					assertThat(resolve(anotherDummyUri)).isNull()
				}
			}

			WebUriRouting {
				route("https://example.com/", sample)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com/"))).isSameInstanceAs(sample)
			}

			WebUriRouting {
				route("https://example.com/*", sample)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com"))).isNull()
				assertThat(resolve(WebUri("https://example.com/"))).isSameInstanceAs(sample)
				assertThat(resolve(WebUri("https://example.com/foo/"))).isSameInstanceAs(sample)
				assertThat(resolve(WebUri("https://example.com/foo/bar"))).isSameInstanceAs(sample)
			}

			WebUriRouting {
				route("sample", foo)
				route("sample*", bar)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("sample"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("sample-entry"))).apply {
					isSameInstanceAs(bar)
					isNotSameInstanceAs(foo)
				}
			}

			WebUriRouting {
				route("https://example.com/*", foo)
				route("https://example.com/foo/", bar)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com/"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("https://example.com/foo/"))).apply {
					isSameInstanceAs(bar)
					isNotSameInstanceAs(foo)
				}
			}

			WebUriRouting {
				route("https://example.com/*", sample)
				route("https://example.com/foo/*", foo)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com"))).isNull()
				assertThat(resolve(WebUri("https://example.com/"))).isSameInstanceAs(sample)
				assertThat(resolve(WebUri("https://example.com/foo/"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("https://example.com/foo/bar"))).apply {
					isSameInstanceAs(foo)
					isNotSameInstanceAs(sample)
				}
			}

			WebUriRouting {
				route("https://example.com/*", sample)
				route("https://example.com/foo/*", foo)
				route("z-baz", baz)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com/"))).isSameInstanceAs(sample)
				assertThat(resolve(WebUri("https://example.com/foo/"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("https://example.com/foo/bar"))).apply {
					isSameInstanceAs(foo)
					isNotSameInstanceAs(sample)
				}
				assertThat(resolve(WebUri("z-baz"))).isSameInstanceAs(baz)
			}

			WebUriRouting {
				route("a://baz/", baz)
				route("https://example.com/*", sample)
				route("https://example.com/foo/*", foo)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com/"))).isSameInstanceAs(sample)
				assertThat(resolve(WebUri("https://example.com/foo/"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("https://example.com/foo/bar"))).apply {
					isSameInstanceAs(foo)
					isNotSameInstanceAs(sample)
				}
				assertThat(resolve(WebUri("a://baz/"))).isSameInstanceAs(baz)
			}

			WebUriRouting {
				route("a-bar", bar)
				route("https://example.com/*", sample)
				route("https://example.com/foo/*", foo)
				route("z://baz/", baz)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com/"))).isSameInstanceAs(sample)
				assertThat(resolve(WebUri("https://example.com/foo/"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("https://example.com/foo/bar"))).apply {
					isSameInstanceAs(foo)
					isNotSameInstanceAs(sample)
				}
				assertThat(resolve(WebUri("a-bar"))).isSameInstanceAs(bar)
				assertThat(resolve(WebUri("z://baz/"))).isSameInstanceAs(baz)
			}

			WebUriRouting {
				route("https://example.com/*", sample)
				route("https://example.com/foo/*", foo)
				route("https://example.com/foo/bar", bar)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com"))).isNull()
				assertThat(resolve(WebUri("https://example.com/"))).isSameInstanceAs(sample)
				assertThat(resolve(WebUri("https://example.com/foo/"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("https://example.com/foo/bar"))).apply {
					isSameInstanceAs(bar)
					isNotSameInstanceAs(foo)
				}
			}

			WebUriRouting {
				route("https://example.com/foo/*", foo)
				route("https://example.com/foo/bar", bar)
				route("https://example.com/foo/bar/baz", baz)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com"))).isNull()
				assertThat(resolve(WebUri("https://example.com/"))).isNull()
				assertThat(resolve(WebUri("https://example.com/foo/"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("https://example.com/foo/bar"))).isSameInstanceAs(bar)
				assertThat(resolve(WebUri("https://example.com/foo/bar/baz"))).apply {
					isSameInstanceAs(baz)
					isNotSameInstanceAs(bar)
				}
			}

			WebUriRouting {
				route("https://example.com/foo/", foo)
				route("https://example.com/foo/bar", bar)
				route("https://example.com/foo/bar/baz", baz)
			}.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com/foo/"))).isSameInstanceAs(foo)
				assertThat(resolve(WebUri("https://example.com/foo/*"))).isNull()
				assertThat(resolve(WebUri("https://example.com/foo/bar"))).isSameInstanceAs(bar)
				assertThat(resolve(WebUri("https://example.com/foo/bar/baz"))).apply {
					isSameInstanceAs(baz)
					isNotSameInstanceAs(bar)
				}
			}

			WebUriRouting().apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com"))).isNull()
				assertThat(resolve(WebUri("https://example.com/"))).isNull()
			}

			WebUriRouting.EMPTY.apply {
				dummies.assertNulls(this)
				assertThat(resolve(WebUri("https://example.com"))).isNull()
				assertThat(resolve(WebUri("https://example.com/"))).isNull()
			}
		}
	}
})
