package org.example.testproject

actual fun getPlatformContext(): Any? = null

actual fun saveFcmToken(userId: String) {
    // iOS 미구현
}