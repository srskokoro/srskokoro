package conv.internal.setup

import com.android.build.api.dsl.ApplicationBaseFlavor
import getOrNull
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.extra

internal fun Project.setUp(android: AndroidExtension): Unit = with(android) {
	val extra = project.extra

	buildToolsVersion = extra.getGradleProp("conv.android.buildToolsVersion")
	compileSdk = extra.getGradleProp("conv.android.compileSdk") { it.toInt() }

	defaultConfig {
		if (this is ApplicationBaseFlavor) {
			targetSdk = extra.getGradleProp("conv.android.targetSdk") { it.toInt() }
		}
		minSdk = extra.getGradleProp("conv.android.minSdk") { it.toInt() }
	}

	@Suppress("UnstableApiUsage")
	testOptions {
		unitTests.all {
			setUp(it)
		}
	}
}

@Suppress("NOTHING_TO_INLINE")
private inline fun ExtraPropertiesExtension.getGradleProp(propName: String) =
	getGradleProp(propName) { it }

private inline fun <R> ExtraPropertiesExtension.getGradleProp(propName: String, transform: (String) -> R) =
	transform(getOrNull<String>(propName) ?: throw E_GradlePropRequired(propName))

private fun E_GradlePropRequired(propName: String) = InvalidUserDataException(
	"Gradle property required: $propName"
)
