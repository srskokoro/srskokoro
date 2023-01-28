plugins {
	`kotlin-dsl`
}

kotlin.sourceSets.main {
	kotlin.srcDir(File(rootDir, "../dependencies"))
}
