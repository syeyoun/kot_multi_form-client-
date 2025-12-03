package org.example.testproject

expect fun getPlatformContext(): Any?
expect fun saveFcmToken(userId: String)  // 추가!