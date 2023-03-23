package com.myapplication

actual fun getPlatformName(): String = "Desktop | ${System.getProperty("java.version")} | ${System.getProperty("java.vm.vendor")}"
