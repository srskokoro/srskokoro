plugins {
	id("build.kt.mpp.inclusive")
	id("build.kt.mpp.android.lib")
}

android {
	namespace = "build.test.android"
}

dependencies {
	commonMainApi("conventions:testing")
	androidMainImplementation("junit:junit")
	androidMainApi("org.robolectric:robolectric")
}
