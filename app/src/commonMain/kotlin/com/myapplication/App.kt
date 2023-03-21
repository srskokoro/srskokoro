package com.myapplication

fun App() {
	println(getPlatformName())
}

expect fun getPlatformName(): String
