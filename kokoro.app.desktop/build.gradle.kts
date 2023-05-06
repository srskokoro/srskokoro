plugins {
	id("conv.kt.jvm.app")
	id("jcef-bundler")
}

application {
	mainClass.set("MainKt")
}

tasks.withType<JavaExec>().configureEach {
	/** @see kokoro.internal.ui.Reflect_InvocationEvent */
	jvmArgs("--add-opens=java.desktop/java.awt.event=ALL-UNNAMED")
}

dependencies {
	deps.bundles.testExtras *= {
		testImplementation(it)
	}
	implementation(project(":kokoro.app"))
	implementation(jcef.dependency)
}
