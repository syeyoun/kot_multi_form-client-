package org.example.testproject

actual fun getPlatformContext(): Any? = null

actual fun saveFcmToken(userId: String) {
    // JS 미구현
}