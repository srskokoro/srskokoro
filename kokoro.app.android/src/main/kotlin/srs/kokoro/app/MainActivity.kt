package srs.kokoro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.myapplication.App

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		App()
	}
}
