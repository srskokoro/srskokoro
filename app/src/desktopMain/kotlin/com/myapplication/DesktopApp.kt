package com.myapplication

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Desktop | ${System.getProperty("java.version")}"

@Preview
@Composable
fun AppPreview() {
	App()
}
