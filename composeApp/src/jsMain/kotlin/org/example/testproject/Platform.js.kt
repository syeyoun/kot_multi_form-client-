package org.example.testproject

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()
actual fun isAndroid(): Boolean = false