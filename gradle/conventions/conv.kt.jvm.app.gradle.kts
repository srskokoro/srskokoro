import conv.internal.setup.*
import org.gradle.jvm.application.tasks.CreateStartScripts

plugins {
	application
	id("conv.kt.jvm")
}

tasks.withType<JavaExec>().configureEach {
	// KLUDGE to force the inclusion of `application.applicationDefaultJvmArgs`,
	//  since `Gradle` seems to set it up via `jvmArguments.convention()` at the
	//  moment.
	jvmArgs = jvmArgs
	// NOTE: It seems that `application.applicationDefaultJvmArgs` is set up via
	// `convention()` now, contrary to what we previously believed, or perhaps,
	// this was introduced to `Gradle` by mistake when `jvmArguments` was
	// introduced (as an alternative to `jvmArgs`). Moreover, the docs isn't
	// even clear about `applicationDefaultJvmArgs` being a "convention" value.
	// - See, https://github.com/gradle/gradle/pull/23924
	// - See also,
	//   - https://github.com/gradle/gradle/issues/15239
	//   - https://github.com/gradle/gradle/issues/13463#issuecomment-1468710781

	setUp(this)
}

tasks.withType<CreateStartScripts>().configureEach {
	setUp(this)
}
