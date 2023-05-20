import conv.internal.setup.*
import org.gradle.jvm.application.tasks.CreateStartScripts

plugins {
	application
	id("conv.kt.jvm")
}

tasks.withType<JavaExec>().configureEach {
	setUp(this)
}

tasks.withType<CreateStartScripts>().configureEach {
	setUp(this)
}
