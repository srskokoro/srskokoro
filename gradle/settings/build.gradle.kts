plugins {
	`kotlin-dsl`
}

group = "convention"

kotlin.sourceSets.main {
	kotlin.srcDir(File(rootDir, "../dependencies"))
}
