import conv.internal.setup.*

plugins {
	application
	id("conv.kt.jvm")
}

tasks.withType<JavaExec>().configureEach {
	setUp(this)
}
