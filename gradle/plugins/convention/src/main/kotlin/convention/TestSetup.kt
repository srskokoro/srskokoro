package convention

import org.gradle.api.tasks.testing.Test

fun Test.setUpTestTask() {
	useJUnitPlatform()
}
