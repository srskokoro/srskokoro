package com.myapplication

import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*

@Composable
fun App() {
	MaterialTheme {
		var text by remember { mutableStateOf("Hello, World!") }

		Button(onClick = {
			text = "Hello, ${getPlatformName()}"
		}) {
			Text(text)
		}
	}
}

expect fun getPlatformName(): String
