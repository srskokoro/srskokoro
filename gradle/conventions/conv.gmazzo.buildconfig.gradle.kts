import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.github.gmazzo.gradle.plugins.BuildConfigTask
import conv.util.*

plugins {
	id("com.github.gmazzo.buildconfig")
}

buildConfig.let {
	val xs = (it as ExtensionAware).extensions
	it.sourceSets.all { this asExtensionIn xs }
}

// Automatically generate build config files on "gradle sync" (via IntelliJ IDEA
// or Android Studio) -- https://twitter.com/Sellmair/status/1619308362881187840
internal val buildConfigTasks = tasks.withType<BuildConfigTask>()
tasks.maybeCreate("prepareKotlinIdeaImport").dependsOn(buildConfigTasks)

// Fixes an issue on Android where a `lintAnalyze*` task would use the output of
// of a `BuildConfigTask` and Gradle would complain since there's neither an
// explicit nor implicit dependency between the said tasks.
// TODO Determine the actual root cause and report it :P
tasks.withType<AndroidLintAnalysisTask>().configureEach { dependsOn(buildConfigTasks) }
